package com.jstart.qypicture.ai.toolCalling;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import com.jstart.qypicture.ai.entity.HistoryMsgEntity;
import com.jstart.qypicture.ai.entity.QwenImageGenerateRequest;
import com.jstart.qypicture.ai.enums.PictureSizeEnum;
import com.jstart.qypicture.enums.PictureStatusEnum;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.vo.PictureUploadVO;
import com.jstart.qypicture.service.PictureService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class GenPictureTool {

    @Value("${api-key.dashscope}")
    private String dashscopeApiKey;

    @Resource
    private ChatClient chatSummaryClient;
    @Resource
    private PictureService pictureService;
    @Resource
    private ChatMemoryRepository chatMemoryRepository;

    @Tool(name = "getPrompt", description = "根据用户输入的描述，获取图片生成的专业提示词")
    public String getPrompt(
            @ToolParam(description = "用户输入的描述，不能为空") String userInput,
            ToolContext toolContext) {
        //1. 从上下文中获取conversationId
        Long conversationId = (Long) toolContext.getContext().get("conversationId");
        log.info("调用getPrompt工具，userInput: {}, conversationId: {}", userInput, conversationId);

        // 如果用户输入为空，返回默认提示词
        if (StrUtil.isBlank(userInput)) {
            return "请用户提供更详细的图片描述";
        }

        //2. 根据conversationId获取聊天记录
        List<Message> historyMessage = chatMemoryRepository.findByConversationId(String.valueOf(conversationId));

        //3. 拼接用户输入和历史记录作为上下文
        String context = userInput;
        if (historyMessage != null && !historyMessage.isEmpty()) {
            //(1)保留最近10条
            historyMessage = historyMessage.subList(Math.max(0, historyMessage.size() - 10), historyMessage.size());
            //(2)提取必要数据
            List<HistoryMsgEntity> result = historyMessage.stream()
                    .map(message -> {
                        String text = message.getText();
                        return new HistoryMsgEntity() {{
                            setMessageType(JSONUtil.parseObj(text).getStr("messageType"));
                            setTextContent(JSONUtil.parseObj(text).getStr("textContent"));
                        }};
                    })
                    .collect(Collectors.toList());
            context = "用户当前输入：" + userInput + "\n历史对话上下文：" + result.toString();
        }

        //4. 调用chatSummaryClient生成专业提示词
        String prompt;
        try {
            prompt = chatSummaryClient.prompt()
                    .user(context)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("调用chatSummaryClient生成图片提示词失败：{}", e.getMessage(), e);
            // 失败时返回用户原始输入作为提示词
            return userInput;
        }
        log.info("获取到图片生成提示词：{}", prompt);
        return prompt;
    }

    @Tool(name = "generateImage", description = "根据提示词生成图片并获取图片URL")
    public String generateImage(
            @ToolParam(description = "图片生成的提示词，不能为空") String prompt,
            @ToolParam(description = "图片比例，默认是1:1", required = false) String scale,
            @ToolParam(description = "反向提示词，不希望出现的内容，例如'畸形'、'残缺'", required = false) String negativePrompt,
            ToolContext toolContext
    ) {
        String pictureSize = PictureSizeEnum.getSizeByScale(scale);
        QwenImageGenerateRequest request = new QwenImageGenerateRequest(
                prompt,
                negativePrompt != null ? negativePrompt : "",
                pictureSize,
                false
        );
        String requestBody = JSONUtil.toJsonStr(request);

        String respBody = "";
        try {
            respBody = HttpUtil.createRequest(Method.POST,
                            "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                    .body(requestBody)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + dashscopeApiKey)
                    .timeout(1000 * 60 * 5)
                    .execute()
                    .body();
        } catch (HttpException e) {
            log.error("请求 阿里百炼 图像生成模型失败：{}", e.getMessage(), e);
            return "图像生成失败，告诉用户稍后重试";
        }
        if (!StrUtil.contains(respBody, "image")) {
            log.error("阿里百炼 图像生成模型返回错误：{}", respBody);
            return "图像生成失败，安抚用户";
        }
        //通过表达式获取JSON中嵌套的对象
        String imageUrl = (String) JSONUtil.getByPath(
                JSONUtil.parse(respBody),
                "output.choices[0].message.content[0].image"
        );

        String finalImageUrl = SaTokenContextMockUtil.setMockContext(() -> {
            String tokenValue = (String) toolContext.getContext().get("tokenValue");
            StpUtil.setTokenValueToStorage(tokenValue);
            try {
                // 上传图片到图床服务
                log.info("开始上传图片到图床服务，图片URL：{}", imageUrl);
                PictureUploadVO uploadResult = pictureService.upload(imageUrl, null);

                PictureEditDTO pictureEditDTO = new PictureEditDTO();
                pictureEditDTO.setId(uploadResult.getId());
                pictureEditDTO.setTags("AI");
                pictureEditDTO.setCategoryId(8L);//添加到AI分类
                pictureEditDTO.setReviewStatus(PictureStatusEnum.PASS.getValue());
                pictureEditDTO.setReviewMessage("AI生成图片，自动审核通过");
                pictureEditDTO.setReviewerId(StpUtil.getLoginIdAsLong());
                pictureService.edit(pictureEditDTO);
                log.info("生成图片成功，图片URL：{}", uploadResult.getThumbUrl());
                return uploadResult.getThumbUrl();
            } catch (Exception e) {
                log.error("上传图片失败：{}", e.getMessage(), e);
                return imageUrl;
            }
        });

        return finalImageUrl;

    }


}
