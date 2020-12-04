package com.javamentor.qa.platform.dao.abstracts.model;

import com.javamentor.qa.platform.models.entity.question.IgnoredTag;
import java.util.*;

public interface IgnoredTagDao extends ReadWriteDao<IgnoredTag, Long>{
    List<IgnoredTag> getIgnoredTagsByUser(String name);

    void addIgnoredTag(IgnoredTag ignoredTag);

    List<IgnoredTag> getIgnoredTagsByPrincipal(Long id);

}
