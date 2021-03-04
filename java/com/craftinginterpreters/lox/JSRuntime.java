package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.craftinginterpreters.lox.JSAtomEnum.*;
import static com.craftinginterpreters.lox.JSAtom.*;

/**
 * @author benpeng.jiang
 * @title: JSRuntime
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/268:15 PM
 */
public class JSRuntime {
  final List<JSString> atomArray;
  final Map<JSString, Integer> atomHash;
  final Map<JSString, JSClass> class_array;

  int stack_size;
  int stack_top;
  JSStackFrame current_stack_frame;

  public JSRuntime() {
    this.atomArray = new ArrayList<>();
    this.atomHash = new HashMap<>();
    this.class_array = new HashMap<>();

    JS_InitAtoms();
  }

  public JSAtom JS_NewAtomStr(String str) {
    return __JS_NewAtom(str, JS_ATOM_TYPE_STRING);
  }

  private JSAtom __JS_NewAtom(String str, int atom_type) {
    JSString p = new JSString(str, atom_type);
    if (atomHash.containsKey(p)) {
      Integer atom = atomHash.get(p);
      return new JSAtom(atom);
    }
    int atomCount = atomArray.size();
    atomHash.put(p, atomCount);
    atomArray.add(p);
    return new JSAtom(atomCount);
  }

  private JSAtom __JS_FindAtom(String str, int atom_type) {
    JSString p = new JSString(str, atom_type);
    if (atomHash.containsKey(p)) {
      Integer atom = atomHash.get(p);
      return new JSAtom(atom);
    }

    return JS_ATOM_NULL;
  }

  private void JS_InitAtoms() {
    atomArray.clear();
    atomHash.clear();
    for(JSAtomEnum atomEnum: JSAtomEnum.values()) {
      System.out.println(atomEnum.name() + ": " + atomEnum.ordinal());
    }

    int atom_type;
    String str;
    for (int i = 0; i < JS_ATOM_END.ordinal(); i++) {
      if (i == JS_ATOM_Private_brand.ordinal()) {
        atom_type = JS_ATOM_TYPE_PRIVATE;
      } else if (i >= JS_ATOM_Symbol_toPrimitive.ordinal()) {
        atom_type = JS_ATOM_TYPE_SYMBOL;
      } else {
        atom_type = JS_ATOM_TYPE_STRING;
      }
      str = js_atom_init.get(i);
      __JS_NewAtom(str, atom_type);
    }
    atomHash.size();
  }
}
