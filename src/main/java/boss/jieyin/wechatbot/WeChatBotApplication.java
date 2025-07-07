package boss.jieyin.wechatbot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("boss.jieyin.wechatbot.mapper")
public class WeChatBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeChatBotApplication.class, args);
    }

}
