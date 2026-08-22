package ee.aleksale.remindly.core.service;

public interface ExtractorService<T, D> {

  T extract(D data);
}
