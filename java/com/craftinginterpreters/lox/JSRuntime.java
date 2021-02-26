package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.craftinginterpreters.lox.AtomEnum.*;
import static com.craftinginterpreters.lox.JSAtom.*;

/**
 * @author benpeng.jiang
 * @title: JSRuntime
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/268:15 PM
 */
public class JSRuntime {
  final List<String> atomArray;
  final Map<String, Integer> atomHash;

  public JSRuntime() {
    this.atomArray = new ArrayList<>();
    this.atomHash = new HashMap<>();

    JS_InitAtoms();
  }

  private String hash_string(String str, int type) {
    return type + ":" + str;
  }

  private JSAtom __JS_NEWAtom(String str, int atom_type) {
    String h = hash_string(str, atom_type);
    if (atomHash.containsKey(h)) {
      Integer atom = atomHash.get(h);
      return new JSAtom(atom);
    }
    int atomCount = atomArray.size();
    atomHash.put(h, atomCount++);
    atomArray.add(h);
    return new JSAtom(atomCount);
  }


  private void JS_InitAtoms() {
    atomArray.clear();
    atomHash.clear();
    for(AtomEnum atomEnum: AtomEnum.values()) {
      System.out.println(atomEnum.name() + ": " + atomEnum.ordinal());
    }

    int atom_type;
    String str;
    for (int i = 1; i < JS_ATOM_END.ordinal(); i++) {
      if (i == JS_ATOM_Private_brand.ordinal()) {
        atom_type = JS_ATOM_TYPE_PRIVATE;
      } else if (i >= JS_ATOM_Symbol_toPrimitive.ordinal()) {
        atom_type = JS_ATOM_TYPE_SYMBOL;
      } else {
        atom_type = JS_ATOM_TYPE_STRING;
      }
      str = js_atom_init.get(i);
      __JS_NEWAtom(str, atom_type);
    }
    atomHash.size();
  }
}
