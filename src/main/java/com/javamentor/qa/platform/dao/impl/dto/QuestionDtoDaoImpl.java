package com.javamentor.qa.platform.dao.impl.dto;

import com.javamentor.qa.platform.dao.abstracts.dto.QuestionDtoDao;
import com.javamentor.qa.platform.models.dto.QuestionDto;
import com.javamentor.qa.platform.models.dto.TagDto;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class QuestionDtoDaoImpl implements QuestionDtoDao {

    @PersistenceContext
    private EntityManager entityManager;

    //Запрос возвращает List QUESTIONDTO
    final String QUERY_QUESTIONDTO ="select question.id as question_id, " +
            " question.title as question_title," +
            "u.fullName as question_authorName," +
            " u.id as question_authorId, " +
            "u.imageLink as question_authorImage," +
            "question.description as question_description," +
            " question.viewCount as question_viewCount," +
            "(select count(a.question.id) from Answer a where a.question.id=:id) as question_countAnswer," +
            "(select count(v.question.id) from VoteQuestion v where v.question.id=:id) as question_countValuable," +
            "question.persistDateTime as question_persistDateTime," +
            "question.lastUpdateDateTime as question_lastUpdateDateTime, " +

            " tag.id as tag_id,tag.name as tag_name " +
            "from Question question  " +
            "INNER JOIN  question.user u" +
            "  join question.tags tag";


    @Override
    public  Optional<QuestionDto> getQuestionDtoById(Long id) {

        return (Optional<QuestionDto>) entityManager.unwrap(Session.class)
                .createQuery(QUERY_QUESTIONDTO+" where question.id=:id")
                .setParameter("id", id)
                .unwrap(org.hibernate.query.Query.class)
                .setResultTransformer(new ResultTransformer() {
                    private Map<Long, QuestionDto> questionDtoMap = new LinkedHashMap<>();

                                @Override
                                public Object transformTuple(Object[] tuple, String[] aliases) {

                                    Map<String,Integer> aliasToIndexMap= aliasToIndexMap(aliases);
                                    Long questionId=((Number) tuple[0]).longValue();

                                    QuestionDto questionDto1 = questionDtoMap.computeIfAbsent(
                                            questionId,
                                            id1 -> {
                                                QuestionDto questionDtoTemp = new QuestionDto();
                                                questionDtoTemp.setId(((Number) tuple[aliasToIndexMap.get("question_id")]).longValue());
                                                questionDtoTemp.setTitle(((String) tuple[aliasToIndexMap.get("question_title")]));

                                                questionDtoTemp.setAuthorName(((String) tuple[aliasToIndexMap.get("question_authorName")]));
                                                questionDtoTemp.setAuthorId(((Number) tuple[aliasToIndexMap.get("question_authorId")]).longValue());
                                                questionDtoTemp.setAuthorImage(((String) tuple[aliasToIndexMap.get("question_authorImage")]));

                                                questionDtoTemp.setDescription(((String) tuple[aliasToIndexMap.get("question_description")]));

                                                questionDtoTemp.setViewCount(((Number) tuple[aliasToIndexMap.get("question_viewCount")]).intValue());
                                                questionDtoTemp.setCountAnswer(((Number) tuple[aliasToIndexMap.get("question_countAnswer")]).intValue());
                                                questionDtoTemp.setCountValuable(((Number) tuple[aliasToIndexMap.get("question_countValuable")]).intValue());

                                                questionDtoTemp.setPersistDateTime((LocalDateTime) tuple[aliasToIndexMap.get("question_persistDateTime")]);
                                                questionDtoTemp.setLastUpdateDateTime((LocalDateTime) tuple[aliasToIndexMap.get("question_lastUpdateDateTime")]);
                                                questionDtoTemp.setListTagDto(new ArrayList<>());
                                                return questionDtoTemp;
                                            }
                                    );


                                    questionDto1.getListTagDto().add(
                                            new TagDto(
                                                    ((Number) tuple[aliasToIndexMap.get("tag_id")]).longValue(),
                                                    ((String) tuple[aliasToIndexMap.get("tag_name")])
                                            )
                                    );

                                    return questionDto1;
                                }


                                @Override
                                public List transformList(List list) {
                                    return new ArrayList<>(questionDtoMap.values());
                                }


                                public  Map<String, Integer> aliasToIndexMap(
                                        String[] aliases) {

                                    Map<String, Integer> aliasToIndexMap = new LinkedHashMap<>();

                                    for (int i = 0; i < aliases.length; i++) {
                                        aliasToIndexMap.put(aliases[i], i);
                                    }

                                    return aliasToIndexMap;
                                }
                            })
                            .uniqueResultOptional();
    }

}