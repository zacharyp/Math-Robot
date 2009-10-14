package org.zachary.mathrobot;

import java.lang.NumberFormatException;

/**
 * Calculator originally coded by Mark Jones as a console based application.
 * I modified it to run through google wave, as well as adding modulus operator.
 */
public class Calculator {
  // Lexical analysis ----------------------------------------------------

  static final char integer = '0';
  static int token;
  static int tokval;
  static int c;

  private int pos;
  private String prob;

  private int getChar() throws NumberFormatException {
    int c = 0;
    try {
      c = (int) prob.charAt(pos);
      ++pos;
    } catch (Exception e) {
      throw new NumberFormatException("Error at position " + pos);
    }
    return c;
  }

  private void getToken() {
    while (Character.isWhitespace((char)c)) { // Skip whitespace
      c = getChar();
    }

    if (c<0 || c == '@') {                // End of input?
      token = 0;
      return;
    }

    switch (c) {                // Special tokens
      case '+' :
      case '-' :
      case '*' :
      case '/' :
      case '%' :
      case '(' :
      case ')' : token = c;
           c   = getChar();
           return;
      default  : if (Character.isDigit((char)c)) {
               int n = 0;
               do {
                 n = 10*n + (c - '0');
                 c = getChar();
               } while (Character.isDigit((char)c));
               tokval = n;
               token  = integer;
               return;
           }
    }
    throw new NumberFormatException("Illegal character "+c);
  }
  
  // Parser --------------------------------------------------------------
  private Expr expr() throws NumberFormatException {        // Expr = Term
    try {
    Expr e = term();          //    | Expr '+' Term
                              //    | Expr '-' Term
    for (;;) {
      if (token=='+') {
        getToken();
        e = new BinExpr('+',e,term());
      } else if (token=='-') {
        getToken();
        e = new BinExpr('-',e,term());
      } else {
          return e;
      }
    }
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
  }

  private Expr term() throws NumberFormatException {          // Term = Atom
    try {
      Expr t = atom();          //    | Term '*' Atom
                        //    | Term '/' Atom
      for (;;) {
        if (token=='*') {
          getToken();
          t = new BinExpr('*',t,atom());
        } else if (token=='/') {
          getToken();
          t = new BinExpr('/',t,atom());
        } else if (token=='%') {
          getToken();
          t = new BinExpr('%',t,atom());  
        } else {
          return t;
        }
      }
    }
    catch (NumberFormatException ex) {
      throw ex;
    }
  }

  private Expr atom() throws NumberFormatException {          // Atom = integer
    if (token=='-') {           //    | '-' Atom
      getToken();
      Expr t = atom();
      return new UnaryExpr('-',t);
    }
    else if (token==integer) {      //    | '(' Expr ')
      int f = tokval;
      getToken();
      return new IntExpr(f);
    } else if (token=='(') {
      getToken();
      Expr e = expr();
      if (token==')') {
      getToken();
      } else {
        throw new NumberFormatException("Missing close paren");
      }
      return e;
    } else {
      throw new NumberFormatException("Syntax error");
    }
  }

  // Representation of expressions ---------------------------------------
  abstract class Expr {
    abstract void compile();
    abstract int  eval();
  }

  private class UnaryExpr extends Expr {
    private char op;
    private Expr value;
    UnaryExpr(char op, Expr value) {
      this.op = op;
      this.value = value;
    }

    void compile() { }

    int eval() {
      int v = value.eval();
      switch (op) {
        case '-' : return - v;
      }
      return 0; /* not used */
    }
  }

  private class IntExpr extends Expr {
    private int value;
    IntExpr(int value) {
      this.value = value;
    }

    void compile() {    }

    int eval() {
      return value;
    }
  }

  private class BinExpr extends Expr {
    private char op;
    private Expr left;
    private Expr right;

    BinExpr(char op, Expr left, Expr right) {
      this.op  = op;
      this.left  = left;
      this.right = right;
    }

    void compile() {    }

    int eval() {
      int l = left.eval();
      int r = right.eval();
      int u = 1;
      switch (op) {
        case '+' : return u * (l + r);
        case '-' : return u * (l - r);
        case '*' : return u * (l * r);
        case '/' : return u * (l / r);
        case '%' : return u * (l % r);
      }
      return 0; /* not used */
    }
  }

  protected Calculator(String problem) {
    this.prob = problem + "@";
    pos = 0;
  }

  // Driver --------------------------------------------------------------
  protected String calc() {
    String response = "";
    try {
      c = getChar();
      getToken();
 
      Expr e = expr();
      int answer = e.eval();
      return response + answer;
    }
    catch (Exception ex) {
      return ex.getMessage();
    }
  }
}
