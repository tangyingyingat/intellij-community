/* The following code was generated by JFlex 1.4 on 3/14/05 5:43 PM */

/* It's an automatically generated code. Do not modify it. */
package com.intellij.lexer;

import com.intellij.psi.tree.IElementType;

import java.io.Reader;

public class _XmlLexer extends FlexAdapter implements ELHostLexer {
  private int myState = __XmlLexer.YYINITIAL;

  public _XmlLexer() {
    super(new __XmlLexer((Reader)null));
  }

  private void packState() {
    final __XmlLexer flex = (__XmlLexer)getFlex();
    myState = ((flex.yyprevstate() & 15) << 4) | (flex.yystate() & 15);
  }

  public void start(char[] buffer, int startOffset, int endOffset, int initialState) {
    super.start(buffer, startOffset, endOffset, initialState);
    handleState(initialState);
  }

  private void handleState(final int initialState) {
    final __XmlLexer flex = (__XmlLexer)getFlex();
    flex.yybegin(initialState & 15);
    flex.pushState((initialState >> 4) & 15);
    packState();
  }

  public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
    super.start(buffer, startOffset, endOffset, initialState);
    handleState(initialState);
  }

  public int getState() {
    return myState;
  }

  public void advance() {
    super.advance();
    packState();
  }

  public void setElTypes(final IElementType jspElContent, final IElementType jspElContent1) {
    ((ELHostLexer)getFlex()).setElTypes(jspElContent, jspElContent1);
  }

  public void setJavaEmbeddedType(final IElementType javaEmbeddedType) {
    ((__XmlLexer)getFlex()).setJavaEmbeddedType(javaEmbeddedType);
  }
}
