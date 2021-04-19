package me.bymartrixx.booklib;

import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BookLib {
    /**
     * Create a new {@link Book.Builder}.
     */
    public static Book.Builder builder() {
        return Book.Builder.create();
    }

    /**
     * Whether a {@link Text} doesn't have style nor siblings (and it isn't a TranslatableText),
     * meaning it can safely be converted to a string without loss of data.
     */
    protected static boolean canTextBeUsedAsString(@Nullable Text text) {
        return text != null && text.getSiblings().size() == 0
                && isStyleEmpty(text.getStyle()) && !(text instanceof TranslatableText);
    }

    /**
     * Whether a style is {@link Style#EMPTY} or everything, but the font, is null.
     */
    protected static boolean isStyleEmpty(Style style) {
        return style.isEmpty() || style.getColor() == null
                && !style.isBold()
                && !style.isItalic()
                && !style.isUnderlined()
                && !style.isStrikethrough()
                && !style.isObfuscated()
                && style.getClickEvent() == null
                && style.getHoverEvent() == null
                && (style.getInsertion() == null || style.getInsertion().isEmpty());
    }

    /**
     * Get a text where the section sign ({@code §}) formatting codes from the provided string are applied.
     */
    protected static Text applyFormattingCodes(String string) {
        Style startingStyle = Style.EMPTY;

        // Read the string until the first formatting change, this part of the string will be our parent text
        AtomicReference<Style> lastStyle = new AtomicReference<>(startingStyle);
        AtomicReference<StringBuilder> currentStr = new AtomicReference<>(new StringBuilder()); // The part of the text currently building
        List<Text> texts = new ArrayList<>(); // The texts that will be joined

        TextVisitFactory.visitFormatted(string, startingStyle, (index, style, codePoint) -> {
            // If the style is not the same as our last style, start with a new text
            if (style != lastStyle.get()) {
                // Don't use an empty parent, but allow the last sibling to be empty
                if (!currentStr.toString().isEmpty() || string.length() == index - 1) {
                    texts.add(new LiteralText(currentStr.get().toString()).setStyle(lastStyle.get()));
                }

                lastStyle.set(style);
                currentStr.set(new StringBuilder());
            }
            currentStr.get().append((char) codePoint);

            return true;
        });

        // We still have some text to append
        if (!currentStr.get().toString().equals("")) {
            texts.add(new LiteralText(currentStr.get().toString()).setStyle(lastStyle.get()));
        }

        Text result = null;
        for (int i = 0; i < texts.size(); ++i) {
            Text text = texts.get(i);
            if (i == 0) {
                result = text;
            } else {
                result = result.shallowCopy().append(text);
            }
        }

        return result != null ? result : new LiteralText("failed");
    }
}
