package com.jstart.qypicture.ai.toolCalling;


import cn.hutool.core.util.StrUtil;
import com.jstart.qypicture.model.dto.PictureEditDTO;
import com.jstart.qypicture.model.dto.PictureQueryListDTO;
import com.jstart.qypicture.model.vo.PictureListVO;
import com.jstart.qypicture.model.vo.PictureVO;
import com.jstart.qypicture.service.PictureService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
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
public class ParsePictureTool {

    @Resource
    private ChatClient parsePictureClient;
    @Resource
    private PictureService pictureService;
    @Resource
    private VectorStore redisVectorStore;

    /**
     * 解析图片
     *
     * @param input
     * @param conversationId
     * @return
     */
    public String act(String input, Long conversationId) {

        PictureVO pictureVO = pictureService.getOneById(conversationId, null);
        if (pictureVO == null) {
            return null;
        }
        String content = pictureVO.getAiDesc();
        if (StrUtil.isBlank(content)) {
            content = parsePictureClient.prompt()
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
            PictureEditDTO pictureEditDTO = new PictureEditDTO();
            pictureEditDTO.setId(pictureVO.getId());
            pictureEditDTO.setAiDesc(content);
            pictureService.edit(pictureEditDTO);

            //todo 因为redisVectorStore.delete()失效，所以暂时放在if里面
            redisVectorStore.add(List.of(
                    Document.builder()
                            .text("图片说明：" + content + "；链接为：" + pictureVO.getThumbUrl())
                            .metadata("picture_id", pictureVO.getId().toString())
                            .metadata("type", "图片推荐")
                            .metadata("action", "发送图片")
                            .build()));

        }



        return content;
    }


}
