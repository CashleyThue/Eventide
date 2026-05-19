# Eventide

Eventide does events

## Usage

add the annotation and register
```java
import com.cashley.Event
import com.cashley.*

public class myClass {
  @Event(eventname, async = <true/false>)
  //Method here

  Eventide.register(new myClass());
}
```

emit the event
```java
Eventide.emit(eventname);
```
