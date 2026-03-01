package com.jstart.qypicture.ai.handle;

import cn.dev33.satoken.stp.StpUtil;
import com.jstart.qypicture.ai.enums.AiActEnum;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.service.PictureService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@Component
public class ParseImgService implements AiActHandler {

    @Resource
    private ChatClient parsePictureClient;
    @Resource
    private PictureService pictureService;
    @Resource
    private VectorStore redisVectorStore;

    @Override
    public String getActName() {
        return AiActEnum.PARSE_IMG.getName();
    }


    /**
     * 图片解析
     *
     * @param input          图片内容
     * @param conversationId 冗余会话id
     * @return 解析结果
     */
    @Override
    public Flux<String> act(String input, Long conversationId) {
        StpUtil.login(2027695570124738561L);//管理员账号
        String content = parsePictureClient.prompt()
                .user(u -> {
                    try {
                        u.text("描述这张图片，用中文详细描述图片的内容")
                                .media(MimeTypeUtils.IMAGE_PNG, new URL(input));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .call()
                .content();
        PictureQueryListDTO pictureQueryListDTO = new PictureQueryListDTO();
        pictureQueryListDTO.setThumbUrl(input);
        PictureListVO picture = pictureService.slectList(pictureQueryListDTO).getFirst();

        if (picture != null){
            PictureEditDTO pictureEditDTO = new PictureEditDTO();
            pictureEditDTO.setId(picture.getId());
            pictureEditDTO.setAiDesc(content);
            pictureService.edit(pictureEditDTO);
        }
        //加入向量数据库
        redisVectorStore.add(List.of(
                new Document(content, Map.of(
                        "type", "图片推荐",
                        "type", "发送图片"))
                )
        );
        return Flux.empty();
    }
}
