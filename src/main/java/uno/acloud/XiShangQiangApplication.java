package uno.acloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@MapperScan("uno.acloud.mapper")
@ServletComponentScan
public class XiShangQiangApplication {

  public static void main(String[] args) {
    SpringApplication.run(XiShangQiangApplication.class, args);
  }

}
