package onenotepicker.demo.com.onenotepickerdemo;

/**
* Created by Michael.Hancock on 10/16/2014.
*/
public interface Callback<T> {
    public void success(T result);

    public void failure(Throwable e);
}
