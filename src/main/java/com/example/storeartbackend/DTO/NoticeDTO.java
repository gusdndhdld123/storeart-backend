package com.example.storeartbackend.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NoticeDTO {

    private Long noticeIdx;

    private String noticeTitle;
    private String noticeContent;

    private LocalDateTime regdate;
    private LocalDateTime moddate;
}
