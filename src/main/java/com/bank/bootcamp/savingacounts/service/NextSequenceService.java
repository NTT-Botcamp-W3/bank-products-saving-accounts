package com.bank.bootcamp.savingacounts.service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import com.bank.bootcamp.savingacounts.entity.Sequence;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NextSequenceService {

  private final ReactiveMongoTemplate mongoTemplate;

  public <T extends Sequence> Mono<Integer> getNextSequence(String seqName)
  {
      var counter = mongoTemplate.findAndModify(
          query(where("_id").is(seqName)),
          new Update().inc("seq",1),
          options().returnNew(true).upsert(true),
          Sequence.class);
      return counter.map(seq -> seq.getSeq());
  }
}
