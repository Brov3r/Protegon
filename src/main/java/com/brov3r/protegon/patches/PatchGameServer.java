package com.brov3r.protegon.patches;

import com.avrix.agent.ClassTransformer;
import com.brov3r.protegon.handlers.OnAddIncomingHandler;
import javassist.CannotCompileException;

/**
 * GameServer patcher
 */
public class PatchGameServer extends ClassTransformer {
    /**
     * Constructor for creating a {@link ClassTransformer} object.
     */
    public PatchGameServer() {
        super("zombie.network.GameServer");
    }

    /**
     * Method for performing class modification.
     * The implementing method must contain the logic for modifying the target class.
     */
    @Override
    public void modifyClass() {
        getModifierBuilder().modifyMethod("addIncoming", (ctClass, ctMethod) -> {
            try {
                ctMethod.insertBefore("{" +
                        "$2.mark();" +
                        OnAddIncomingHandler.class.getName() + ".handlePacket($1, $2, $3);" +
                        "$2.reset();" +
                        "if (" + OnAddIncomingHandler.class.getName() + ".isShouldBlock()) return;" +
                        "}");
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
    }
}