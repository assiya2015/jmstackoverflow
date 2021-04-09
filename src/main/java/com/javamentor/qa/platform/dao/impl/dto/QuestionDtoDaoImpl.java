package com.javamentor.qa.platform.dao.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.QuestionDtoDao;
import com.javamentor.qa.platform.dao.impl.dto.transformers.QuestionResultTransformer;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class QuestionDtoDaoImpl implements QuestionDtoDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<QuestionDto> getQuestionDtoById(Long id) {

        return (Optional<QuestionDto>) entityManager.unwrap(Session.class)
                .createQuery("select question.id as question_id, " +
                        " question.title as question_title," +
                        "u.fullName as question_authorName," +
                        " u.id as question_authorId, " +
                        "u.imageLink as question_authorImage," +
                        "question.description as question_description," +
                        " question.viewCount as question_viewCount," +
                        "(select count(a.question.id) from Answer a where a.question.id=:id) as question_countAnswer," +
                        "(select coalesce(sum(v.vote), 0) from VoteQuestion v where v.question.id=:id) as question_countValuable," +
                        "question.persistDateTime as question_persistDateTime," +
                        "question.lastUpdateDateTime as question_lastUpdateDateTime, " +
                        " tag.id as tag_id,tag.name as tag_name, tag.description as tag_description " +
                        "from Question question  " +
                        "INNER JOIN  question.user u" +
                        "  join question.tags tag"
                        + " where question.id=:id")
                .setParameter("id", id)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new QuestionResultTransformer())
                .uniqueResultOptional();

    }

    @Override
    public List<Long> getPaginationQuestionIdsWithoutAnswerOrderByNew(int page, int size) {
        return   (List<Long>) entityManager
                .createQuery("select q.id from Question q left outer join Answer a on (q.id = a.question.id) where a.question.id is null order by q.persistDateTime desc")
                .setFirstResult(page * size - size)
                .setMaxResults(size)
                .getResultList();
    }
}