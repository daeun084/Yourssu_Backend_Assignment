## 🌿 유어슈 과제 기록
2024 하반기 유어슈 Backend 리크루팅 과제 기록 Repository
2024.09.13 ~ 2024.09.16

<br><br>

## Stack
**Language & Framework**  
<img src="https://img.shields.io/badge/Java-007396?style=flat&logo=Java&logoColor=white" />
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat&logo=SpringBoot&logoColor=white" /> 
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat&logo=SpringSecurity&logoColor=white" />


**Documentation&Test**  
<img src="https://img.shields.io/badge/Rest Docs-6DB33F?style=flat&logo=Spring&logoColor=white" /> 

**Database & ORM**  
<img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=flat&logo=Spring&logoColor=white" /> 
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=MySQL&logoColor=white" /> 

**Build Tool**  
<img src="https://img.shields.io/badge/Gradle-02303A?style=flat&logo=Gradle&logoColor=white" />


<br><br>

## API
|   Method   |   Domain    |   URI   |                                                                                            
| :--------: | :---------: | :-------: |
|   POST     |     USER     |  `/api/v1/sign-up`    |
|   POST     |     USER     |  `/api/v1/sign-in`    |
|   DELETE   |     USER     |  `/api/v1/withdrawal`  |
|   POST     |    ARTICLE   |  `/api/v1/article`    |
|   PATCH    |    ARTICLE   |  `/api/v1/article`    |
|   DELETE   |    ARTICLE   |  `/api/v1/article`   |
|   POST     |    COMMENT   |  `/api/v1/comment`    |
|   PATCH    |    COMMENT   |  `/api/v1/comment`    |
|   DELETE   |    COMMENT   |  `/api/v1/comment`    |


<br><br>

## Directory
```PlainText
src/
├── docs/
├── main/
│   ├── common/
│   │ ├── exception/
│   │ ├── response/
│   │ ├── status/
│   │ ├── base/
│   │ ├── security/
│   │ └── config/
│   ├── domain/
│   │  ├── entity/
│   │  ├── controller/
│   │  ├── service/
│   │  ├── repository/
│   │  ├── converter/
│   │  └── dto/
│       ├── request/
│       └── response/
├── test/

		 
```

