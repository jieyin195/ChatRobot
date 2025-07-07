package boss.jieyin.wechatbot.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.lang.reflect.Member;


public interface MemberMapper {

    @Select("SELECT * FROM member WHERE member_id = #{memberId} AND is_deleted = 0 LIMIT 1")
    Member findByMemberId(@Param("memberId") String memberId);

    @Insert("INSERT INTO member(member_id, member_name, member_level, created_at, updated_at, is_deleted) " +
            "VALUES(#{memberId}, #{memberName}, #{memberLevel}, NOW(), NOW(), 0)")
    void insert(Member member);
}
