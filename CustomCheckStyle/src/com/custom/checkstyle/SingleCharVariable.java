
package com.custom.checkstyle;

import java.util.ArrayList;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class SingleCharVariable
    extends AbstractCheck
{
    /**
     * Types of nodes interested in
     */
    public int[] getDefaultTokens()
    {
        return new int[]
        {
            TokenTypes.CLASS_DEF,
            TokenTypes.ENUM_CONSTANT_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.FOR_EACH_CLAUSE,
            TokenTypes.FOR_INIT,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.PARAMETER_DEF,
            TokenTypes.VARIABLE_DEF,
        };
    }

    /**
     * Called when a node is entered.  Generates name tree.
     */
    public void visitToken(DetailAST ast)
    {
        try
        {
            DetailAST name =  ast.findFirstToken(TokenTypes.IDENT);

            if(debug)
            {
                nest += "  ";
                System.err.print(nest + "+ " + ast.getText());
                if(name != null) System.err.print(" " + name.getText());
                System.err.println();
            }

            switch(ast.getType())
            {
                case TokenTypes.CLASS_DEF:					// fall through to PARAMETER_DEF
                case TokenTypes.ENUM_CONSTANT_DEF:
                case TokenTypes.ENUM_DEF:
                case TokenTypes.FOR_EACH_CLAUSE:
                case TokenTypes.FOR_INIT:
                case TokenTypes.INTERFACE_DEF:
                case TokenTypes.METHOD_DEF:
                case TokenTypes.PARAMETER_DEF:
                    if(name != null)							// skip Java 5 for(i:x) that have null name
                        checkName(name);
                    break;

                case TokenTypes.VARIABLE_DEF:
                    checkName(name);
                    break;
            }
        }
        catch(Exception e)
        {
            System.err.println("Processing file " + getFileContents().getFileName() + ":" + ast.getLineNo() + ":" + ast.getColumnNo());
            e.printStackTrace();
        }
   }

    private boolean isAllowed(String name)
    {
        boolean rval = false;
        for (String allowed:mAllowList)
        {
            if (name.equals(allowed))
            {
                rval = true;
            }
        }
        return rval;
    }

    /**
     * Checks a name for more than one character
     */
    private void checkName(DetailAST name)
    {
        if((name.getText().length() == 1) && (!isAllowed(name.getText())))
        {
            Object [] data = new Object[1];
            data[0] = name.getText();
            log(name.getLineNo(), name.getColumnNo(), "SingleCharVariable", data);
        }
    }

    /**
     * Called when a node is exited.  Checks names when a method,. constructor,
     * and top level class.
     */
    public void leaveToken(DetailAST ast)
    {
        try
        {
            DetailAST name =  ast.findFirstToken(TokenTypes.IDENT);

            if(debug)
            {
                nest = nest.substring(2);
                System.err.print(nest + "- " + ast.getText());
                if(name != null) System.err.print(" " + name.getText());
                System.err.println();
            }

            switch(ast.getType())
            {
                case TokenTypes.FOR_EACH_CLAUSE:		// fall through to FOR_INIT
                case TokenTypes.FOR_INIT:
                    break;
            }
        }
        catch(Exception e)
        {
            System.err.println("Processing file " + getFileContents().getFileName() + ":" + ast.getLineNo() + ":" + ast.getColumnNo());
            e.printStackTrace();
        }
   }

    /**
     * Sets list of allowable variable names
     * @param flag check only if set to true
     */
    public void setAllowList(String list)
    {
        mAllowList = list.split(",");
    }

    /**
     * The tokens that this check must be registered for.
     * @return the token set this must be registered for.
     * @see TokenTypes
     */
    @Override
    public int[] getRequiredTokens() {
        int arr[] = new int[0];
        return arr;
    }

    /**
     * The configurable token set.
     * Used to protect Checks against malicious users who specify an
     * unacceptable token set in the configuration file.
     * The default implementation returns the check's default tokens.
     * @return the token set this check is designed for.
     * @see TokenTypes
     */
    @Override
    public int[] getAcceptableTokens() {
        final int[] defaultTokens = getDefaultTokens();
        final int[] copy = new int[defaultTokens.length];
        System.arraycopy(defaultTokens, 0, copy, 0, defaultTokens.length);
        return copy;
    }

    /** list of allowable single character names */
    private String[] mAllowList;

    /** debug flag */
    private static boolean debug = false;
    /** nesting prefix */
    private String nest = "";
}
