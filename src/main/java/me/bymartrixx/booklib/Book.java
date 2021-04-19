package me.bymartrixx.booklib;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class that holds pages and can be converted to a {@link Items#WRITTEN_BOOK} or a
 * {@link Items#WRITABLE_BOOK}.
 */
public class Book {
    private final List<Page> pages;

    private Book(List<Page> pages) {
        if (pages.size() == 0) {
            throw new IllegalArgumentException("The book must not be empty!");
        }
        this.pages = pages;
    }

    /**
     * Get a list containing the pages that this book has.
     */
    public List<Page> getPages() {
        return this.pages;
    }

    /**
     * Get the page at the specified index.
     *
     * @param page the index of the page to get
     * @return the page
     */
    public Page getPage(int page) {
        return this.pages.get(page);
    }

    /**
     * Get the number of pages this book holds.
     */
    public int getPageCount() {
        return this.pages.size();
    }

    /**
     * Create a new {@linkplain Items#WRITABLE_BOOK writable book} with the pages that this Book
     * contains.
     *
     * @return a {@linkplain Items#WRITABLE_BOOK writable book} with the NBT data of the pages.
     */
    public ItemStack toWritableBook() {
        ItemStack item = new ItemStack(Items.WRITABLE_BOOK);
        item.setTag(this.toTag());
        return item;
    }

    /**
     * Create a new {@linkplain Items#WRITTEN_BOOK written book} with the pages that this Book
     * contains, and the provided author and title.
     *
     * @param author a name that will appear as the pplayer that signed this book
     * @param title the title that the book will have
     * @return a {@linkplain Items#WRITTEN_BOOK written book} with the NBT data of the pages,
     * author and title.
     */
    public ItemStack toWrittenBook(String title, String author) {
        ItemStack item = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = this.toTag();
        tag.put("author", StringTag.of(author));
        tag.put("title", StringTag.of(title));

        item.setTag(tag);
        return item;
    }

    /**
     * Set the pages data of a given {@linkplain Items#WRITABLE_BOOK writable book} or
     * {@linkplain Items#WRITTEN_BOOK written book} to the pages that this book holds.
     *
     * @param stack the item to add the pages to
     * @return the given item, with the pages NBT data
     */
    public ItemStack addPagesToStack(ItemStack stack) {
        if (stack.getItem() != Items.WRITABLE_BOOK && stack.getItem() != Items.WRITTEN_BOOK) {
            throw new IllegalArgumentException("The item of the stack must be a written or writable book");
        }

        if (stack.hasTag()) {
            stack.putSubTag("pages", this.toTag().get("pages"));
        } else {
            stack.setTag(this.toTag());
        }

        return stack;
    }

    private CompoundTag toTag() {
        ListTag pagesTag = new ListTag();
        for (Page page : this.pages) {
            pagesTag.add(StringTag.of(page.toJson()));
        }

        CompoundTag tag = new CompoundTag();
        tag.put("pages", pagesTag);
        return tag;
    }

    @Override
    public String toString() {
        return "Book[pageCount=" + this.getPageCount() + ", pages="+ this.pages +"]";
    }

    static class Page {
        private final Text content;

        private Page(Text content) {
            this.content = content;
        }

        /**
         * Get the content of this page.
         */
        public Text getContent() {
            return this.content;
        }

        /**
         * Get the content of this page serialized as a JSON String.
         */
        public String toJson() {
            return Text.Serializer.toJson(this.content);
        }

        @Override
        public String toString() {
            return this.toJson();
        }

        /**
         * Whether the contents of this page are empty.
         */
        public boolean isEmpty() {
            return this.content.asString().isEmpty();
        }

        // Should only be accessible by Book$Builder
        private static class Builder {
            private String content = "";

            private Builder() {
            }

            private void append(String text) {
                // If there are no contents, use the provided text as content
                if (this.content.isEmpty()) {
                    this.content = Text.Serializer.toJson(BookLib.applyFormattingCodes(text));
                    return;
                }

                Text text1 = BookLib.applyFormattingCodes(text);
                MutableText current = Text.Serializer.fromJson(this.content);

                // Try to use the content and the provided text as strings
                if (BookLib.canTextBeUsedAsString(current) && BookLib.canTextBeUsedAsString(text1)) {
                    this.content = Text.Serializer.toJson(new LiteralText(current.asString() + text1.asString()));
                } else {
                    this.content = Text.Serializer.toJson(current != null ? current.append(text1) : text1);
                }
            }

            private void append(Text text) {
                // If there are no contents, use the provided text as content
                if (this.content.isEmpty()) {
                    this.content = Text.Serializer.toJson(text);
                    return;
                }

                MutableText current = Text.Serializer.fromJson(this.content);

                if (BookLib.canTextBeUsedAsString(current) && BookLib.canTextBeUsedAsString(text)) {
                    this.content = Text.Serializer.toJson(new LiteralText(current.asString() + text.asString()));
                } else {
                    this.content = Text.Serializer.toJson(current != null ? current.append(text) : text);
                }
            }

            private Page build() {
                if (this.content.length() > Short.MAX_VALUE) {
                    throw new IllegalStateException("Page contents too long, must be less than 32767 (" + this.content.length() + ")");
                }

                Text content = Text.Serializer.fromJson(this.content);
                return new Page(content != null ? content : new LiteralText(""));
            }
        }
    }

    /**
     * A {@link Book} builder class to easily create new Books. Currently it doesn't have support
     * for automatic line wraps or new pages, so be careful with how many content you use.
     */
    public static class Builder {
        private final List<Page.Builder> pages = new ArrayList<>();

        private Builder() {
            this.pages.add(new Page.Builder());
        }

        /**
         * Create a new Builder.
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Build a {@link Book} with the content that has been provided. Empty pages after the last
         * one with content are removed.
         */
        public Book build() {
            List<Page> pages = this.pages.stream().map(Page.Builder::build).collect(Collectors.toCollection(ArrayList::new));

            // Trim pages
            for (int i = pages.size() - 1; i > 0; --i) {
                Page page = pages.get(i);

                if (page.isEmpty()) {
                    pages.remove(page);
                } else {
                    break;
                }
            }

            return new Book(pages);
        }

        /**
         * Append a String to the latest page. Has support for
         * <a href="https://minecraft.fandom.com/wiki/Formatting_codes">Formatting codes</a>.
         *
         * @param text the string to append
         * @see #appendText(Text)
         */
        public Builder append(String text) {
            this.pages.get(this.pages.size() - 1).append(text);
            return this;
        }

        /**
         * Append a String to the latest page and start a new line. Has support for
         * <a href="https://minecraft.fandom.com/wiki/Formatting_codes">Formatting codes</a>.
         *
         * @param text the string to append
         * @see #appendText(Text)
         */
        public Builder appendLine(String text) {
            return this.append(text + "\n");
        }

        /**
         * Add a number of new lines to the current page.
         *
         * @param lines the number of lines to add. Must be greater than 0.
         */
        public Builder newLines(int lines) {
            if (lines <= 0) {
                throw new IllegalArgumentException("The number of lines must be greater than 0");
            }

            return this.append(new String(new char[lines]).replace("\0", "\n"));
        }

        /**
         * Start/add a new line on the current page.
         */
        public Builder newLine() {
            return this.newLines(1);
        }

        /**
         * Add a number of empty pages to the book.
         *
         * @param pages the number of pages to add
         */
        public Builder newPages(int pages) {
            if (pages <= 0) {
                throw new IllegalArgumentException("The number of pages must be greater than 0");
            }

            for (int i = 1; i <= pages; ++i) {
                this.pages.add(new Page.Builder());
            }
            return this;
        }

        /**
         * Start/add an empty page to the book.
         */
        public Builder newPage() {
            return this.newPages(1);
        }

        /**
         * Append Text to the current page. Useful for things that can't be added with {@link #append(String)},
         * like a {@link net.minecraft.text.ClickEvent} or {@link net.minecraft.text.HoverEvent}.
         *
         * @param text the text to append
         */
        public Builder appendText(Text text) {
            this.pages.get(this.pages.size() - 1).append(text);
            return this;
        }
    }
}
