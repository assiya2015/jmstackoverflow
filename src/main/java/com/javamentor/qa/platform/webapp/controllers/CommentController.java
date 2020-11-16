package com.javamentor.qa.platform.webapp.controllers;


import com.javamentor.qa.platform.models.dto.CommentDto;
import com.javamentor.qa.platform.models.entity.question.CommentQuestion;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.CommentAnswer;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.service.abstracts.model.*;
import com.javamentor.qa.platform.webapp.converters.CommentConverter;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Validated
@RequestMapping("/api/comment/")
@Api(value = "CommentApi")
public class CommentController {

    private final QuestionService questionService;
    private final CommentQuestionService commentQuestionService;
    private final UserService userService;
    private final CommentConverter commentConverter;
    private final AnswerService answerService;
    private final CommentAnswerService commentAnswerService;

    public CommentController(QuestionService questionService,
                             CommentQuestionService commentQuestionService,
                             UserService userService,
                             CommentConverter commentConverter,
                             AnswerService answerService,
                             CommentAnswerService commentAnswerService) {

        this.questionService = questionService;
        this.commentQuestionService = commentQuestionService;
        this.userService = userService;
        this.commentConverter = commentConverter;
        this.answerService = answerService;
        this.commentAnswerService = commentAnswerService;
    }


    @PostMapping("question/{questionId}")
    @ApiOperation(value = "Add comment", notes = "This method Add comment to question and return CommentDto")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Comment was added", response = CommentDto.class),
            @ApiResponse(code = 400, message = "Question or user not found", response = String.class)
    })

    public ResponseEntity<?> addCommentToQuestion(
            @ApiParam(name = "QuestionId", value = "QuestionId. Type long", required = true, example = "1")
            @PathVariable Long questionId,

            @ApiParam(name = "UserId", value = "UserId. Type long", required = true, example = "1")
            @RequestParam Long userId,

            @ApiParam(name = "text", value = "Text of comment. Type string", required = true, example = "Some comment")
            @RequestBody String commentText) {


        Optional<User> user = userService.getById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Optional<Question> question = questionService.getById(questionId);
        if (!question.isPresent()) {
            return ResponseEntity.badRequest().body("Question not found");
        }

        CommentQuestion commentQuestion =
                commentQuestionService.addCommentToQuestion(commentText, question.get(), user.get());

        return ResponseEntity.ok(commentConverter.commentToCommentDTO(commentQuestion));
    }

    @PostMapping("answer/{answerId}")
    @ApiOperation(value = "Add comment", notes = "This method Add comment to answer and return CommentDto")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Comment was added", response = CommentDto.class),
            @ApiResponse(code = 400, message = "Answer or user not found", response = String.class)
    })
    public ResponseEntity<?> addCommentToAnswer(
            @ApiParam(name = "AnswerId", value = "AnswerId. Type long", required = true, example = "1")
            @PathVariable Long answerId,
            @ApiParam(name = "UserId", value = "UserId. Type long", required = true, example = "1")
            @RequestParam Long userId,

            @ApiParam(name = "text", value = "Text of comment. Type string", required = true, example = "Some comment")
            @RequestBody String commentText) {

        Optional<User> user = userService.getById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Optional<Answer> answer = answerService.getById(answerId);
        if (!answer.isPresent()) {
            return ResponseEntity.badRequest().body("Answer not found");
        }

        CommentAnswer commentAnswer = commentAnswerService.addCommentToAnswer(commentText, answer.get(), user.get());

        return ResponseEntity.ok(commentConverter.commentToCommentDTO(commentAnswer));
    }
}