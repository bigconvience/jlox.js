package com.lox.javascript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lox.javascript.JS_PROP.JS_PROP_CONFIGURABLE;
import static com.lox.javascript.JS_PROP.JS_PROP_WRITABLE;


/**
 * @author benpeng.jiang
 * @title: FunctionDef
 * @projectName LoxScript
 * @description: TODO
 * @date 2021/2/228:17 PM
 */ //< stmt-expression
//> stmt-function
public class JSFunctionDef extends Stmt {
  JSContext ctx;
  JSFunctionDef parent;
  int parent_cpool_idx;

  int parent_scope_level;
  final List<JSFunctionDef> child_list;

  final List<JSValue> cpool;
  final List<LabelSlot> label_slots;
  int last_opcode_line_num = -1;
  int last_opcode_pos = -1;
  int line_num;
  String filename;

  final Token name = null;
  final List<Token> params;

  final List<JSVarScope> scopes;
  int scope_level;
  int scope_first;

  final Map<String, JSVarDef> varDefMap;
  final List<JSVarDef> vars;
  final List<JSVarDef> args;
  final List<JSHoistedDef> hoisted_def;
  final List<JSClosureVar> closure_var;
  final Map<String, JSHoistedDef> hoistDef;
  List<Stmt> body;
  int decl_line_number;
  int leave_line_number;
  int eval_type;
  boolean is_eval;
  boolean is_global_var;
  JSVarScope curScope;
  DynBuf byte_code;
  byte js_mode;
  JSAtom func_name;

  JumpSlot[] jump_slots;
  int jump_size;
  int jump_count;
  int defined_arg_count;

  boolean has_simple_parameter_list;

  LineNumberSlot[] line_number_slots;
  int line_number_size;
  int line_number_count;
  int line_number_last;
  int line_number_last_pc;

  int var_object_idx = -1;
  int func_var_idx = -1;
  boolean has_arguments_binding;
  boolean is_func_expr;
  boolean has_this_binding;
  boolean is_derived_class_constructor;

  int arguments_var_idx = -1;;
  int home_object_var_idx = -1;
  int this_active_func_var_idx = -1;;
  int new_target_var_idx = -1;
  int eval_ret_idx = -1;
  int this_var_idx = -1;;

  boolean has_prototype;
  boolean has_home_object;
  boolean new_target_allowed;
  boolean super_call_allowed;
  boolean super_allowed;
  boolean arguments_allowed;
  boolean in_function_body;

  JSFunctionKindEnum func_kind = JSFunctionKindEnum.JS_FUNC_NORMAL;
  JSParseFunctionEnum func_type = JSParseFunctionEnum.JS_PARSE_FUNC_STATEMENT;

  DynBuf pc2line;
  int source_len;
  String source;
  JSModuleDef module;
  BlockEnv top_break;
  boolean has_eval_call;

  JSFunctionDef(JSFunctionDef parent,
                boolean is_eval, boolean isFuncExpr, String filename, int lineNum) {
    this.parent = parent;
    this.parent_cpool_idx = -1;
    if (parent != null) {
      parent.child_list.add(this);
      js_mode = parent.js_mode;
      parent_scope_level = parent.scope_level;;
    }
    this.is_eval = is_eval;

    params = new ArrayList<>();
    varDefMap = new HashMap<>();
    hoistDef = new HashMap<>();
    scopes = new ArrayList<>();
    vars = new ArrayList<>();
    hoisted_def = new ArrayList<>();
    args = new ArrayList<>();
    closure_var = new ArrayList<>();
    child_list = new ArrayList<>();
    cpool = new ArrayList<>();
    label_slots = new ArrayList<>();

  }

  @Override
  <R> R accept(Visitor<R> visitor) {
    return visitor.visitFunctionStmt(this);
  }


  JSHoistedDef findHoistedDef(Token name) {
    return hoistDef.get(name.lexeme);
  }

  int getScopeCount() {
    return scopes.size();
  }

  int add_scope() {
    int scope = getScopeCount();
    JSVarScope varScope = new JSVarScope();
    scopes.add(varScope);
    varScope.parent = scope_level;
    varScope.first = scope_first;
    scope_level = scope;
    return scope;
  }

  void pop_scope() {
    int scope = scope_level;
    scope_level = scopes.get(scope).parent;
    scope_first = get_first_lexical_var(this, scope_level);
  }

   static int get_first_lexical_var(JSFunctionDef fd, int scope)
  {
    while (scope >= 0) {
      int scope_idx = fd.scopes.get(scope).first;
      if (scope_idx >= 0)
        return scope_idx;
      scope = fd.scopes.get(scope).parent;
    }
    return -1;
  }

  public JSVarDef findLexicalDef(JSAtom varName) {
    JSVarScope scope = curScope;
    while (scope != null) {
      JSVarDef varDef = scope.get(varName);
      if (varDef != null && varDef.is_lexical) {
        return varDef;
      }
      scope = scope.prev;
    }

    if (is_eval && eval_type == LoxJS.JS_EVAL_TYPE_GLOBAL) {
      return findLexicalHoistedDef(varName);
    }
    return null;
  }

  public JSHoistedDef findLexicalHoistedDef(JSAtom varName) {
    JSHoistedDef hoistedDef = findHoistedDef(varName);
    if (hoistedDef != null && hoistedDef.is_lexical) {
      return hoistedDef;
    }
    return null;
  }

  JSHoistedDef findHoistedDef(JSAtom varName) {
    for (JSHoistedDef hf : hoisted_def) {
      if (hf.var_name.equals(varName)) {
        return hf;
      }
    }
    return null;
  }

  public JSVarDef findVarInChildScope(JSAtom name) {
    for (JSVarDef vd : vars) {
      if (vd != null && vd.var_name.equals(name) && vd.scope_level == 0) {
        if (isChildScope(vd.func_pool_or_scope_idx, scope_level)) {
          return vd;
        }
        return vd;
      }
    }

    return null;
  }

  public boolean isChildScope(int scope, int parentScope) {
    while (scope > 0) {
      if (scope == parentScope) {
        return true;
      }
      scope = scopes.get(scope).parent;
    }
    return false;
  }

  public static boolean add_hoisted_def(JSContext ctx, JSFunctionDef fd, int cpool_idx, JSAtom var_name,
                                        int var_Idx,
                                        boolean is_lexical) {
    return fd.addHoistedDef(cpool_idx, var_name, var_Idx, is_lexical) != null;
  }

  public JSHoistedDef addHoistedDef(int cpoolIdx, JSAtom varName,
                                    int varIdx,
                                    boolean isLexical) {
    JSHoistedDef hf = new JSHoistedDef();
    hoisted_def.add(hf);
    hf.var_name = varName;
    hf.cpool_idx = cpoolIdx;
    hf.is_lexical = isLexical;
    hf.force_init = false;
    hf.var_idx = varIdx;
    hf.scope_level = scope_level;
    return hf;
  }


  public int findVar(JSAtom varName) {
    for (int i = 0; i < vars.size(); i++) {
      JSVarDef vd = vars.get(i);
      if (vd.var_name.equals(varName) && vd.scope_level == 0) {
        return i;
      }
    }

    return findArg(varName);
  }

  public int findArg(JSAtom varName) {
    for (int i = 0; i < args.size(); i++) {
      JSVarDef vd = args.get(i);
      if (vd.var_name.equals(varName)) {
        return i | JSVarDef.ARGUMENT_VAR_OFFSET;
      }
    }
    return -1;
  }

  void enter_scope(int scope, DynBuf bcOut) {
    JSFunctionDef s = this;
    if (scope == 1) {

    }

    for (int scopeIdx = s.scopes.get(scope).first; scopeIdx >= 0; ) {
      JSVarDef vd = s.vars.get(scopeIdx);
      if (vd.scope_level == scopeIdx) {
        if (isFuncDecl(vd.var_kind)) {
          bcOut.dbuf_putc(OPCodeEnum.OP_fclosure);
          bcOut.dbuf_put_u32(vd.func_pool_or_scope_idx);
          bcOut.dbuf_putc(OPCodeEnum.OP_put_loc);
        } else {
          bcOut.dbuf_putc(OPCodeEnum.OP_set_loc_uninitialized);
        }
        bcOut.dbuf_put_u16((short) scopeIdx);
        scopeIdx = vd.scope_next;
      } else {
        break;
      }
    }
  }


  void instantiate_hoisted_definitions(DynBuf bc) {
    JSFunctionDef s = this;
    int i, idx, var_idx;
    for (i = 0; i < s.hoisted_def.size(); i++) {
      JSHoistedDef hf = s.hoisted_def.get(i);
      int has_closure = 0;
      boolean force_init = hf.force_init;
      if (s.is_global_var && hf.var_name != JSAtom.JS_ATOM_NULL) {
        for (idx = 0; idx < s.closure_var.size(); idx++) {
          JSClosureVar cv = s.closure_var.get(idx);
          if (hf.var_name.equals(cv.var_name)) {
            has_closure = 2;
            force_init = false;
            break;
          }
        }
        if (has_closure == 0) {
          int flags = 0;
          if (s.eval_type != LoxJS.JS_EVAL_TYPE_GLOBAL) {
            flags |= JS_PROP_CONFIGURABLE;
          }

          if (hf.cpool_idx >= 0 && !hf.is_lexical) {
            bc.dbuf_putc(OPCodeEnum.OP_fclosure);
            bc.dbuf_put_u32(hf.cpool_idx);
            bc.dbuf_putc(OPCodeEnum.OP_define_func);
            bc.put_atom(hf.var_name);
            bc.dbuf_putc(flags);
            continue;
          } else {
            if (hf.is_lexical) {
              flags |= JSContext.DEFINE_GLOBAL_LEX_VAR;
              if (!hf.is_const) {
                flags |= JS_PROP_WRITABLE;
              }
            }
            bc.dbuf_putc(OPCodeEnum.OP_define_var);
            bc.put_atom(hf.var_name);
            bc.dbuf_putc(flags);
          }
        }

        if (hf.cpool_idx >= 0 || force_init) {
          if (hf.cpool_idx >= 0) {
            bc.dbuf_putc(OPCodeEnum.OP_fclosure);
            bc.dbuf_put_u32(hf.cpool_idx);
            if (hf.var_name.getVal() == JSAtomEnum.JS_ATOM__default_.ordinal()) {
              /* set default export function name */
              bc.dbuf_putc(OPCodeEnum.OP_set_name);
              bc.put_atom(hf.var_name);
            }
          } else {
            bc.dbuf_putc(OPCodeEnum.OP_undefined);
          }
          if (s.is_global_var) {
            if (has_closure == 2) {
              bc.dbuf_putc(OPCodeEnum.OP_put_var_ref);
              bc.dbuf_put_u16(idx);
            } else if (has_closure == 1) {
              bc.dbuf_putc(OPCodeEnum.OP_define_field);
              bc.put_atom(hf.var_name);
              bc.dbuf_putc(OPCodeEnum.OP_drop);
            } else {
              /* XXX: Check if variable is writable and enumerable */
              bc.dbuf_putc(OPCodeEnum.OP_put_var);
              bc.put_atom(hf.var_name);
            }
          } else {
            var_idx = hf.var_idx;
            if ((var_idx & JSVarDef.ARGUMENT_VAR_OFFSET) != 0) {
              bc.dbuf_putc(OPCodeEnum.OP_put_arg);
              bc.dbuf_put_u16(var_idx - JSVarDef.ARGUMENT_VAR_OFFSET);
            } else {
              bc.dbuf_putc(OPCodeEnum.OP_put_loc);
              bc.dbuf_put_u16(var_idx);
            }
          }
        }
      }
    }
    s.hoisted_def.clear();
  }

  public static boolean isFuncDecl(JSVarKindEnum varKind) {
    return varKind == JSVarKindEnum.JS_VAR_FUNCTION_DECL ||
      varKind == JSVarKindEnum.JS_VAR_NEW_FUNCTION_DECL;
  }

  int new_label_fd() {
    return new_label_fd(-1);
  }

  public static int update_label(JSFunctionDef s, int label, int delta) {
    return s.update_label(label, delta);
  }

  int update_label(int label, int delta) {
    LabelSlot ls = label_slots.get(label);
    ls.ref_count += delta;
    return ls.ref_count;
  }

  int new_label_fd(int label) {
    LabelSlot ls;
    if (label < 0) {
      label = label_slots.size();
      ls = new LabelSlot();
      label_slots.add(ls);
      ls.ref_count = 0;
      ls.pos = -1;
      ls.pos2 = -1;
      ls.addr = -1;
    }
    return label;
  }


  OPCodeEnum get_prev_code() {
    if (last_opcode_pos < 0) {
      return OPCodeEnum.OP_invalid;
    }
    return OPCodeEnum.values()[byte_code.get_byte(last_opcode_pos)];
  }
}
