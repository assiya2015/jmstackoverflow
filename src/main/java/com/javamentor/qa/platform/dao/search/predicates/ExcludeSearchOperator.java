package com.javamentor.qa.platform.dao.search.predicates;

import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExcludeSearchOperator extends SearchOperator {
    protected ExcludeSearchOperator(@Value("exclude (-) search operator") String description,
                                    @Value("5") int order) {
        super(description, order);
    }

    @Override
    public BooleanPredicateClausesStep<?> parse(StringBuilder query, SearchPredicateFactory factory, BooleanPredicateClausesStep<?> booleanPredicate) {
        Pattern pattern = Pattern.compile("(-)([а-яА-Яa-zA-Z0-9\\-]+)");
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {
            BooleanPredicateClausesStep<?> innerBooleanPredicate = factory.bool()
                    .should(factory.match().field("title").matching(matcher.group(2)))
                    .should(factory.match().field("description").matching(matcher.group(2)))
                    .should(factory.match().field("htmlBody").matching(matcher.group(2)));
            booleanPredicate = booleanPredicate.mustNot(innerBooleanPredicate);
        }

        query.replace(0, query.length(), matcher.replaceAll(""));

        return booleanPredicate;
    }
}