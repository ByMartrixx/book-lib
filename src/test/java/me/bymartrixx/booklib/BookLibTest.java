package me.bymartrixx.booklib;

import net.minecraft.text.Text;
import org.junit.Assert;
import org.junit.Test;

public class BookLibTest {
    @Test
    public void testFormattingCodes() {
        Text text1 = BookLib.applyFormattingCodes("§e§nThis is some sample coloured and underlined §rtext!");
        Text text2 = BookLib.applyFormattingCodes("This §eis §c§nnot §r§kobfuscated");
        Text text3 = BookLib.applyFormattingCodes("Lorem §9ipsum§2§n dolor §osit amet,§r consectetur");
        Assert.assertEquals(1, text1.getSiblings().size());
        Assert.assertEquals("This is some sample coloured and underlined ", text1.asString());
        Assert.assertEquals(3, text2.getSiblings().size());
        Assert.assertEquals("obfuscated", text2.getSiblings().get(2).asString());
        Assert.assertEquals(4, text3.getSiblings().size());
        Assert.assertEquals("sit amet,", text3.getSiblings().get(2).asString());
    }

    @Test
    public void testBookBuilder() {
        Book book = Book.Builder.create().append("Hello").newLine().append("world!").build();
        Book toTrim = Book.Builder.create().append("Hello world!").newPages(4).build();
        Book toNotTrim = Book.Builder.create().append("Hello world!").newPages(4).append("Hello world!").build();
        Assert.assertEquals("Hello\nworld!", book.getPage(0).getContent().asString());
        Assert.assertEquals(1, toTrim.getPageCount());
        Assert.assertEquals(5, toNotTrim.getPageCount());
    }
}
