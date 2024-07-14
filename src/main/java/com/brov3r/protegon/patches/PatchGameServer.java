package com.brov3r.protegon.patches;

import com.avrix.agent.ClassTransformer;
import com.brov3r.protegon.modules.BrushToolAC;
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
        getModifierBuilder().modifyMethod("receiveAddItemToMap", (ctClass, ctMethod) -> {
            try {
                ctMethod.insertBefore("{" +
                        "if (!" + BrushToolAC.class.getName() + ".handlePacket($1, $2)) return;" +
                        "$1.rewind();" +
                        "}");
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
    }
}