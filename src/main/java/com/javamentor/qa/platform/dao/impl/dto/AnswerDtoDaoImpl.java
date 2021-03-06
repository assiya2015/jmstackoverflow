package com.javamentor.qa.platform.dao.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.AnswerDtoDao;
import com.javamentor.qa.platform.models.dto.AnswerDto;
import com.javamentor.qa.platform.models.entity.question.Question;
import com.javamentor.qa.platform.models.entity.question.answer.Answer;
import com.javamentor.qa.platform.models.entity.question.answer.VoteAnswer;
import com.javamentor.qa.platform.webapp.converters.AnswerConverter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AnswerDtoDaoImpl implements AnswerDtoDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AnswerDto> getAllAnswersByQuestionId(Long questionId) {

        return (List<AnswerDto>) entityManager.unwrap(Session.class)
                .createQuery("SELECT new com.javamentor.qa.platform.models.dto.AnswerDto(a.id, u.id, q.id, " +
                        "a.htmlBody, a.persistDateTime, a.isHelpful, a.dateAcceptTime, " +
                        "(SELECT SUM (av.vote ) FROM VoteAnswer AS av WHERE av.answer.id = a.id), " +
                        "u.imageLink, u.fullName) " +
                        "FROM Answer as a " +
                        "INNER JOIN a.user as u " +
                        "JOIN a.question as q " +
                        "WHERE q.id = :questionId and a.isDeletedByModerator = false")
                .setParameter("questionId", questionId)
                .unwrap(org.hibernate.query.Query.class)
                .getResultList();
    }

    @Override
    public boolean isUserAlreadyAnsweredToQuestion(Long userId, Long questionId) {
        return (entityManager.unwrap(Session.class)
                .createQuery("FROM Answer a WHERE question.id =: questionId" +
                        " and user.id =: userId and a.isDeletedByModerator = false")
                .setParameter("questionId", questionId)
                .setParameter("userId", userId)
                .unwrap(org.hibernate.query.Query.class)
                .uniqueResult() != null);
    }
}
