# Book Lib

A simple Minecraft library to ease creation of Vanilla books.

## Usage
The usage is fairly simple
```java
Book book = BookLib.create().appendLine("Hello world!").appendText(new TranslatableText("translation.key"));
ItemStack bookItem = book.toWritableBook();
```


## License

This mod is available under the MIT license. You can modify and distribute it as long as you preserve copyright and license notices.
