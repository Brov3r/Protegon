package com.brov3r.protegon.patches;

import com.avrix.agent.ClassTransformer;
import com.brov3r.protegon.modules.ChatModule;
import javassist.CannotCompileException;

/**
 * ChatServer patcher
 */
public class PatchChatServer extends ClassTransformer {
    /**
     * Constructor for creating a {@link ClassTransformer} object.
     */
    public PatchChatServer() {
        super("zombie.network.chat.ChatServer");
    }

    /**
     * Method for performing class modification.
     * The implementing method must contain the logic for modifying the target class.
     */
    @Override
    public void modifyClass() {
        getModifierBuilder().modifyMethod("processMessageFromPlayerPacket", (ctClass, ctMethod) -> {
            try {
                ctMethod.insertBefore("{" +
                        "int index = $1.getInt();" +
                        "synchronized(chats) {" +
                        "zombie.chat.ChatBase chatBase = (zombie.chat.ChatBase) chats.get(Integer.valueOf(index));" +
                        "zombie.chat.ChatMessage message = chatBase.unpackMessage($1);" +
                        "if (!" + ChatModule.class.getName() + ".handleChatMessage(chatBase, message)) return;" +
                        "}" +
                        "$1.rewind();" +
                        "}");
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
    }
}