package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.JSAtom.JS_ATOM_TYPE_STRING;

/**
 * @author benpeng.jiang
 * @title: AtomUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/38:56 AM
 */
public class JSAtomUtils {
  static JSValue JS_AtomToValue(JSContext ctx, JSAtom atom) {
    return __JS_AtomToValue(ctx, atom, false);
  }

  static JSValue JS_AtomToString(JSContext ctx, JSAtom atom) {
    return __JS_AtomToValue(ctx, atom, true);
  }

  private static JSValue __JS_AtomToValue(JSContext ctx, JSAtom atom, boolean force_string) {
    if (atom.__JS_AtomIsTaggedInt()) {
     int u32 = atom.__JS_AtomToUInt32();
     return JSValue.JS_NewString(ctx, Utils.intToByteArray(u32));
    } else {
      JSRuntime rt = ctx.rt;
      JSString p = rt.atomArray.get(atom.getVal());
      if (p.atom_type == JS_ATOM_TYPE_STRING) {
        return new JSValue(JSTag.JS_TAG_STRING, p);
      } else if (force_string) {
        if (p.str == null) {
          p = rt.atomArray.get(JSAtomEnum.JS_ATOM_empty_string.ordinal());
        }
        return new JSValue(JSTag.JS_TAG_STRING, p);
      } else {
        return new JSValue(JSTag.JS_TAG_SYMBOL, p);
      }
    }
  }
}
