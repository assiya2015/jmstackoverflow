package com.javamentor.qa.platform.webapp.converters;


import com.javamentor.qa.platform.models.dto.VoteAnswerDto;
import com.javamentor.qa.platform.models.entity.question.answer.VoteAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class VoteAnswerConverter {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "answer.id", target = "answerId")
    @Mapping(source = "persistDateTime", target = "persistDateTime")
    @Mapping(source = "vote", target = "vote")
    public abstract VoteAnswerDto voteAnswerToVoteAnswerDto (VoteAnswer voteAnswer);
 }
