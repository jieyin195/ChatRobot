package boss.jieyin.wechatbot.mapper;

import boss.jieyin.wechatbot.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


@Mapper
public interface ChatMessageMapper {

    // 根据 sessionId 查询消息
//    List<ChatMessage> findMessagesBySessionId(@Param("sessionId") String sessionId);

    // 插入一条消息
    int insert(ChatMessage chatMessage);

    @Select("SELECT * FROM chat_message " +
            "WHERE status = 0 and robot_id=#{robotId} "+
            "ORDER BY message_id ASC "+
            "LIMIT #{limit}")
    List<ChatMessage> fetchPulledMessages(@Param("limit") int limit, @Param("robotId") String robotId);

    void markMessagesAsPulled(@Param("ids") List<Long> ids);

    @Update({
            "<script>",
            "UPDATE chat_message SET status = #{status}, updated_at = NOW() WHERE message_id IN ",
            "<foreach collection='messageIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    void updateStatusByMessageIds(@Param("messageIds") List<String> messageIds, @Param("status") int status);


}
