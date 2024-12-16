package com.example.storeartbackend.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentsDTO {
    //댓글 id
    private Long commentIdx;
    // 게시판 id v
    private Long callcenterIdx;
    //내용 v
    private String comment;
    //글쓴이. 그냥 글쓴이의 등급에 따라 작성자/관리자로 나누기 v
    private String writer;

    private LocalDateTime regdate;
    private LocalDateTime moddate;
}
