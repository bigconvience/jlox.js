package com.lox.javascript;

import static com.lox.javascript.JSThrower.js_parse_error;
import static com.lox.javascript.JSVarDefEnum.*;
import static com.lox.javascript.JSVarDefEnum.JS_VAR_DEF_CONST;
import static com.lox.javascript.JSVarKindEnum.*;

/**
 * @author benpeng.jiang
 * @title: JSVarUtils
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/3/262:22 PM
 */
public class JSVarUtils {

  public static int define_var(Resolver s, JSFunctionDef fd,
                               JSAtom varName, JSVarDefEnum varDefType) {
    JSVarDef vd;
    JSHoistedDef hf;
    JSContext ctx = s.ctx;
    int idx = -1;
    switch (varDefType) {
      case JS_VAR_DEF_LET:
      case JS_VAR_DEF_CONST:
      case JS_VAR_DEF_CATCH:
        vd = fd.findLexicalDef(varName);
        if (vd != null) {
          if (!vd.isGlobalVar) {
            if (vd.scope == fd.curScope) {
              js_parse_error(s, "invalid redefinition of lexical identifier");
            }
          } else {
            if (fd.scope_level == 1) {
              js_parse_error(s, "invalid redefinition of lexical identifier");
            }
          }
        }

        if (fd.findVarInChildScope(varName) != null) {
          js_parse_error(s, "invalid redefinition of variable, find a scope caterpillar");
        }

        if (fd.is_global_var) {
          hf = fd.findHoistedDef(varName);
          if (hf != null && fd.isChildScope(hf.scope_level, fd.scope_level)) {
            js_parse_error(s, "invalid redefinition of global identifier");
          }
        }

        if (fd.is_eval &&
          (fd.eval_type == LoxJS.JS_EVAL_TYPE_GLOBAL ||
            fd.eval_type == LoxJS.JS_EVAL_TYPE_MODULE)
          && fd.scope_level == 1) {
          hf = fd.addHoistedDef(-1, varName, -1, true);
          hf.is_const = varDefType == JS_VAR_DEF_CONST;
          idx = JSVarDef.GLOBAL_VAR_OFFSET;
        } else {
          JSVarKindEnum varKind;
          if (varDefType == JS_VAR_DEF_FUNCTION_DECL)
            varKind = JS_VAR_FUNCTION_DECL;
          else if (varDefType == JS_VAR_DEF_NEW_FUNCTION_DECL)
            varKind = JS_VAR_NEW_FUNCTION_DECL;
          else
            varKind = JS_VAR_NORMAL;
          idx = JSVarDef.add_scope_var(ctx, fd, varName, varKind);
          vd = fd.vars.get(idx);
          if (vd != null) {
            vd.is_lexical = true;
            vd.is_const = varDefType == JS_VAR_DEF_CONST;
          }
        }
        break;

      case JS_VAR_DEF_VAR:
        vd = fd.findLexicalDef(varName);
        if (vd != null) {
          invalid_lexical_redefinition:
          js_parse_error(s, "invalid redefinition of lexical identifier");
        }
        if (fd.is_global_var) {
          hf = fd.findHoistedDef(varName);
          if (hf != null && hf.is_lexical
            && hf.scope_level == fd.scope_level && fd.eval_type == LoxJS.JS_EVAL_TYPE_MODULE) {
            js_parse_error(s, "invalid redefinition of lexical identifier");
          }
          hf = fd.addHoistedDef(-1, varName, -1, false);
          idx = JSVarDef.GLOBAL_VAR_OFFSET;
        } else {
          idx = fd.findVar(varName);
          if (idx >= 0) {
            break;
          }
          idx = JSVarDef.add_var(ctx, fd, varName);
          if (idx >= 0) {
            vd = fd.vars.get(idx);
            vd.func_pool_or_scope_idx = fd.scope_level;
          }
        }
        break;
    }

    return idx;
  }


}
