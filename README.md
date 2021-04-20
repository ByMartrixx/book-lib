# Book Lib

A simple Minecraft library to ease creation of Vanilla books.

## Usage
To use it, add this to your repositories block on your build.gradle
```gradle
repositories {
    maven {
        name 'ByMartrixx Maven'
        url 'https://maven.bymartrixx.me'
    }
}
```
And to the dependencies block
```gradle
dependencies {
    modImplementation 'me.bymartrixx:book-lib:1.0.0'
    // Optional, for JiJ
    include 'me.bymartrixx:book-lib:1.0.0'
}
```


The usage is fairly simple
```java
Book book = BookLib.create().appendLine("Hello world!").appendText(new TranslatableText("translation.key"));
ItemStack bookItem = book.toWritableBook();
```


## License

This mod is available under the MIT license. You can modify and distribute it as long as you preserve copyright and license notices.
