package is.trinit.sepp.commands;

import com.mojang.brigadier.context.CommandContext;
import is.trinit.sepp.latex.LatexGenerator;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;

public class MathCommands {

    public static int latex(CommandContext<MessageCommandSource> context) {
        var content = context.getArgument("text", String.class);
        var latexFile = LatexGenerator.getInstance().formulaToImage(content);
        var channelMono = context.getSource().getMessage().getChannel();
        Mono.justOrEmpty(latexFile)
                .flatMap(stream -> channelMono.flatMap(channel -> channel.createMessage(spec -> spec.addFile("upload.png", stream)))
                    .doFinally(signal -> disposeInputStream(stream)))
                .switchIfEmpty(channelMono.flatMap(c -> c.createMessage("bad input")))
                .subscribe();

        return 0;
    }

    private static void disposeInputStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

}
