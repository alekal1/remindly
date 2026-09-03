package ee.aleksale.remindly.core.service;

public interface ExtractorService<RESULT, INPUT> {

  RESULT extract(INPUT data);
}
