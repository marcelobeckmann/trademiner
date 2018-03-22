package com.rapidminer.operator.preprocessing.transformation;

import java.util.StringTokenizer;
import java.util.Vector;
public class SentenceStorer
{

    private Vector lastSentences;
    private Vector fileStorer;
    private Vector currentVector;
    private Vector lastSentence;
    private Vector lastHTML;
    private Vector currentHTMLVec;
    private boolean inMarkup;
    private boolean inHref;
    private boolean endOfSentence;
    private boolean keepSpaces;
    private boolean is_all_markup;
    private String newLine;

    public SentenceStorer()
    {
        lastSentences = null;
        fileStorer = null;
        currentVector = null;
        lastSentence = null;
        lastHTML = null;
        currentHTMLVec = null;
        inMarkup = false;
        inHref = false;
        endOfSentence = false;
        keepSpaces = true;
        is_all_markup = true;
        newLine = System.getProperty("line.separator");
        lastSentences = new Vector();
        fileStorer = new Vector();
        currentHTMLVec = new Vector();
        addStorage();
    }

    public void addLine(String s, boolean flag)
    {
        keepSpaces = flag;
        addLine(s);
    }

    public void addLine(String s)
    {
        endOfSentence = false;
        if(!isWhiteSpaceOnly(s))
        {
            is_all_markup = true;
            endOfSentence = false;
            addSentence(s);
            if(!is_all_markup)
            {
                addItem(newLine, currentVector.size());
            }
        } else
        {
            endOfSentence = true;
            addStorage();
        }
    }

    private boolean isWhiteSpaceOnly(String s)
    {
        return s.equals(newLine) || s.equals(" ") || s.equals(System.getProperty("line.separator"));
    }

    public void addTerm(String s, boolean flag)
    {
        keepSpaces = flag;
        addTerm(s);
    }

    public void addTerm(String s)
    {
        endOfSentence = false;
        addSentence(s);
        addStorage();
    }

    public Vector hrefsOnly()
    {
        Vector vector = new Vector();
        if(lastHTML != null)
        {
            for(int i = 0; i < lastHTML.size(); i++)
            {
                String s = lastHTML.elementAt(i).toString();
                if(s.startsWith("href") || s.startsWith("src") || s.startsWith("HREF") || s.startsWith("SRC") || s.startsWith("\"") && s.endsWith("\"") || s.startsWith("\"") && s.endsWith(">"))
                {
                    vector.addElement(s);
                }
            }

        }
        return vector;
    }

    public boolean eos_found()
    {
        return endOfSentence;
    }

    public Vector getSentence()
    {
        return lastSentence;
    }

    public Vector getTerm()
    {
        return lastSentence;
    }

    public void addSentence(String s, boolean flag)
    {
        keepSpaces = flag;
        addSentence(s);
    }

    public void addSentence(String s)
    {
        StringTokenizer stringtokenizer = new StringTokenizer(s, " \n");
        do
        {
            if(!stringtokenizer.hasMoreTokens())
            {
                break;
            }
            String s1 = stringtokenizer.nextToken();
            addToken(s1, currentVector.size(), currentHTMLVec.size());
            if(!inMarkup && !inHref && keepSpaces)
            {
                addItem(" ", currentVector.size());
            }
        } while(true);
    }

    public Vector getAllSentences()
    {
        return lastSentences;
    }

    public void doneSentences()
    {
        lastSentences = new Vector();
    }

    private void addStorage()
    {
        if(currentVector != null)
        {
            lastSentence = currentVector;
            lastHTML = currentHTMLVec;
            lastSentences.addElement(currentVector);
        }
        currentVector = new Vector();
        fileStorer.addElement(currentVector);
    }

    private void writeUrls()
    {
        Vector vector = hrefsOnly();
        for(int i = 0; i < vector.size(); i++)
        {
            System.out.println(vector.elementAt(i));
        }

    }

    private void addHTMLItem(String s, int i)
    {
        currentHTMLVec.insertElementAt(s, i);
        s = s.toLowerCase();
        if(s.equals("<br>") || s.equals("<p>") || s.equals("<hr>") || s.equals("<li>") || s.equals("</html>") || s.equals("</head>") || s.equals("</title>") || s.equals("</font>") || s.equals("</body>"))
        {
            endOfSentence = true;
            addStorage();
        }
        if(s.equals("</a>"))
        {
            inHref = false;
        }
    }

    private boolean endSentenceCondition(String s)
    {
        return s.endsWith("!") || s.endsWith("?") || s.lastIndexOf(".") == s.length() - 1;
    }

    private boolean notEndSentenceWord(String s)
    {
        return s.toLowerCase().equals("mr.") || s.toLowerCase().equals("vs.") || s.toLowerCase().equals("corp.");
    }

    private boolean isStartRemovableChar(char c)
    {
        switch(c)
        {
        case 34: // '"'
        case 39: // '\''
            return true;
        }
        return false;
    }

    private boolean isEndSentenceChar(char c)
    {
        switch(c)
        {
        case 33: // '!'
        case 46: // '.'
        case 63: // '?'
            return true;
        }
        return false;
    }

    private boolean isEndRemovableChar(char c)
    {
        switch(c)
        {
        case 34: // '"'
        case 44: // ','
        case 58: // ':'
        case 59: // ';'
            return true;
        }
        return false;
    }

    private boolean isEndableItem(String s)
    {
        return s.equalsIgnoreCase("mr.") || s.equalsIgnoreCase("mrs.") || s.equalsIgnoreCase("u.s.") || s.equalsIgnoreCase("u.k.") || s.equalsIgnoreCase("jan.") || s.equalsIgnoreCase("feb.") || s.equalsIgnoreCase("mar.") || s.equalsIgnoreCase("apr.") || s.equalsIgnoreCase("jun.") || s.equalsIgnoreCase("jul.") || s.equalsIgnoreCase("aug.") || s.equalsIgnoreCase("sep.") || s.equalsIgnoreCase("oct.") || s.equalsIgnoreCase("nov.") || s.equalsIgnoreCase("dec.") || s.equalsIgnoreCase("mrs.") || s.indexOf(".") != s.length() - 1 && !Character.isDigit(s.charAt(0));
    }

    private boolean isAcronym(String s)
    {
        return isEndableItem(s);
    }

    private void addItem(String s)
    {
        if(!inHref)
        {
            int i = s.length();
            char c = s.charAt(0);
            char c1 = s.charAt(i - 1);
            if(isStartRemovableChar(c))
            {
                currentVector.addElement(s.substring(0, 1));
                if(i > 1)
                {
                    addItem(s.substring(1, i));
                }
            } else
            if(isEndRemovableChar(c1))
            {
                if(i > 1)
                {
                    addItem(s.substring(0, i - 1));
                }
                currentVector.addElement(s.substring(i - 1, i));
            } else
            if(isEndSentenceChar(c1) && !isAcronym(s))
            {
                if(i > 1)
                {
                    addItem(s.substring(0, i - 1));
                }
                currentVector.addElement(s.substring(i - 1, i));
                is_all_markup = false;
                endOfSentence = true;
                addStorage();
            } else
            {
                currentVector.addElement(s);
            }
        }
    }

    private void addItem(String s, int i)
    {
        addItem(s);
    }

    private void addToken(String s, int i, int j)
    {
        int k = s.length();
        if(s.length() > 0)
        {
            char c = s.charAt(0);
            switch(c)
            {
            case 36: // '$'
            case 39: // '\''
            case 40: // '('
            case 43: // '+'
            case 91: // '['
            case 96: // '`'
                removeMarkup(s.substring(0, 1), currentVector.size(), currentHTMLVec.size());
                addToken(s.substring(1, k), currentVector.size(), currentHTMLVec.size());
                break;

            default:
                addWord(s, i, j);
                break;
            }
        }
    }

    private boolean getChars(String s, int i)
    {
        char c = s.charAt(i);
        switch(c)
        {
        case 36: // '$'
        case 39: // '\''
        case 41: // ')'
        case 59: // ';'
        case 93: // ']'
        case 96: // '`'
            return false;
        }
        return true;
    }

    private void addWord(String s, int i, int j)
    {
        int k = 0;
        String s1 = "";
        String s2 = "";
        for(; k < s.length() && getChars(s, k); k++)
        {
            s1 = s1 + s.charAt(k);
        }

        removeMarkup(s1, currentVector.size(), j);
        if(k < s.length())
        {
            removeMarkup(s.substring(k, k + 1), currentVector.size(), j);
            addWord(s.substring(k + 1, s.length()), currentVector.size(), j);
        }
    }

    private void addLessStartPunct(String s, int i, int j)
    {
        int k;
        if((k = s.length()) > 0)
        {
            char c = s.charAt(s.length() - 1);
            switch(c)
            {
            case 33: // '!'
            case 36: // '$'
            case 39: // '\''
            case 41: // ')'
            case 44: // ','
            case 58: // ':'
            case 61: // '='
            case 63: // '?'
            case 93: // ']'
            case 96: // '`'
                addLessStartPunct(s.substring(0, k - 1), currentVector.size(), j);
                break;

            default:
                removeMarkup(s, i, j);
                break;
            }
        }
    }

    private void removeMarkup(String s, int i, int j)
    {
        int k = s.length() - 1;
        int l = s.indexOf('<');
        int i1 = s.indexOf('>');
        if(k >= 0)
        {
            if(l == 0)
            {
                if(i1 == k)
                {
                    addHTMLItem(s, currentHTMLVec.size());
                } else
                if(i1 == -1)
                {
                    inMarkup = true;
                    addHTMLItem(s, currentHTMLVec.size());
                } else
                if(i1 < k)
                {
                    addHTMLItem(s.substring(0, i1 + 1), currentHTMLVec.size());
                    removeMarkup(s.substring(i1 + 1, s.length()), i, j);
                }
            } else
            if(l == -1)
            {
                if(i1 == k)
                {
                    inMarkup = false;
                    addHTMLItem(s, currentHTMLVec.size());
                } else
                if(i1 == -1 && inMarkup)
                {
                    addHTMLItem(s, currentHTMLVec.size());
                } else
                if(i1 == -1)
                {
                    testCompoundToken(s, currentVector.size());
                } else
                if(i1 < k && i1 != -1)
                {
                    inMarkup = false;
                    addHTMLItem(s.substring(0, i1 + 1), currentHTMLVec.size());
                    removeMarkup(s.substring(i1 + 1, s.length()), currentVector.size(), currentHTMLVec.size());
                }
            } else
            if(l > 0 && i1 == -1)
            {
                inMarkup = true;
                testCompoundToken(s.substring(0, l), currentVector.size());
                removeMarkup(s.substring(l, s.length()), currentVector.size(), j);
            } else
            if(l > 0 && l < i1)
            {
                testCompoundToken(s.substring(0, l), currentVector.size());
                removeMarkup(s.substring(l, s.length()), currentVector.size(), j);
            } else
            if(l > i1 && i1 > -1)
            {
                inMarkup = false;
                addHTMLItem(s.substring(0, i1 + 1), currentHTMLVec.size());
                removeMarkup(s.substring(i1 + 1, s.length()), currentVector.size(), j);
            }
        }
    }

    private String replaceHTMLItems(String s)
    {
        String s1 = "&quot;";
        String s2 = "&amp;";
        String s3 = "&nbsp;";
        int i;
        if((i = s.indexOf(s1)) > -1)
        {
            return s.substring(0, i) + "\"" + replaceHTMLItems(s.substring(i + s1.length(), s.length()));
        }
        if((i = s.indexOf(s2)) > -1)
        {
            return s.substring(0, i) + "&" + replaceHTMLItems(s.substring(i + s2.length(), s.length()));
        }
        if((i = s.indexOf(s3)) > -1)
        {
            return s.substring(0, i) + "" + replaceHTMLItems(s.substring(i + s3.length(), s.length()));
        } else
        {
            return s;
        }
    }

    private void testCompoundToken(String s, int i)
    {
        s = replaceHTMLItems(s);
        addItem(s, i);
    }
}
