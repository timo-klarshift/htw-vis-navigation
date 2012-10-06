package org.htw.vis.db.big;

public interface BigMapIteratorCallback<T> {
	public void onKeyValue(Integer key, T value);
}
