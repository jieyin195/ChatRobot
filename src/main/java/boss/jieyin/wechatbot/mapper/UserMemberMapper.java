package boss.jieyin.wechatbot.mapper;

import boss.jieyin.wechatbot.model.UserMemberEntity;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMemberMapper {
    @Select("SELECT * " +
            "        FROM user_member " +
            "        WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0 " +
            "        ORDER BY created_at DESC " +
            "        LIMIT 1 ")
    UserMemberEntity findByUserId(@Param("userId") String userId);

    @Update("UPDATE user_member" +
            "        SET available_times = available_times - 1," +
            "            updated_at = NOW()" +
            "        WHERE user_id = #{userId}" +
            "          AND available_times > 0" +
            "          AND status = 1" +
            "          AND is_deleted = 0")
    int decreaseAvailableTimes(@Param("userId") String userId);

    @Insert(" INSERT INTO user_member ( " +
            "            user_id, " +
            "            member_id, " +
            "            created_at, " +
            "            updated_at" +
            "        ) VALUES ( " +
            "            #{userId}, " +
            "            #{memberId}, " +
            "            NOW(), " +
            "            NOW())")
    int insert(UserMemberEntity entity);
}
