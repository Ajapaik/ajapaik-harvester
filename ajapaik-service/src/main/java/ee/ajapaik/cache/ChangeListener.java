package ee.ajapaik.cache;

import java.util.List;

public interface ChangeListener<T> {

	void notify(List<T> data);
}
