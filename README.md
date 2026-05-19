# Eventide

Eventide does events

## Build

```bash
./gradlew build
```

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
after you're finished you can shut it down
```java
Eventide.shutdown();
```
