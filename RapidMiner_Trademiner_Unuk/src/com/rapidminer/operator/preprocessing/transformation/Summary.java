package com.rapidminer.operator.preprocessing.transformation;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class Summary
{

    int opening;
    int open_sent;
    int closing;
    int close_sent;
    int min_diff;
    int central;
    int max_bonds;
    int bond_thresh;
    int numSentences;
    double top;
    double mid;
    double bottom;
    Hashtable fastSum;
    Hashtable bonded;
    Vector allSorted;
    Vector sentences;
    Vector bonds;
    Vector closedClassWords;
    boolean on_web;
    String result;
    String nl;
    Summarise summ;

    public Summary(boolean flag, double d, double d1, double d2, 
            int i)
    {
        opening = 0;
        open_sent = 0;
        closing = 0;
        close_sent = 0;
        min_diff = 0;
        central = 0;
        max_bonds = 0;
        bond_thresh = 3;
        numSentences = 0;
        fastSum = new Hashtable();
        bonded = new Hashtable();
        allSorted = new Vector();
        sentences = new Vector();
        bonds = new Vector();
        on_web = false;
        result = "";
        nl = System.getProperty("line.separator");
        on_web = flag;
        top = d;
        mid = d1;
        bottom = d2;
        bond_thresh = i;
    }

    public Summary()
    {
        opening = 0;
        open_sent = 0;
        closing = 0;
        close_sent = 0;
        min_diff = 0;
        central = 0;
        max_bonds = 0;
        bond_thresh = 3;
        numSentences = 0;
        fastSum = new Hashtable();
        bonded = new Hashtable();
        allSorted = new Vector();
        sentences = new Vector();
        bonds = new Vector();
        on_web = false;
        result = "";
        nl = System.getProperty("line.separator");
    }

    public void setTopicOpening(double d)
    {
        top = d;
    }

    public void setTopicCentral(double d)
    {
        mid = d;
    }

    public void setTopicClosing(double d)
    {
        bottom = d;
    }

    public void setBondThreshold(int i)
    {
        bond_thresh = i;
    }

    public String getResult()
    {
        determine_output();
        return result;
    }

    public String result()
    {
        return result;
    }

    public void printSentence(Vector vector)
    {
        for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements(); System.out.println(enumeration.nextElement().toString())) { }
    }

    public void addSentence(Vector vector, int i)
    {
        sentences.addElement(vector);
        Vector vector1 = createStoredWords(vector);
        Vector vector2 = createFerretedWords(vector);
        vector1 = append(vector1, vector2);
        summ = new Summarise(vector, vector1, i);
        addFastSum(vector1, summ);
        allSorted.addElement(summ);
    }

    public void addSentence(Vector vector, Vector vector1)
    {
        sentences.addElement(vector);
        summ = new Summarise(vector, vector1, numSentences);
        addFastSum(vector1, summ);
        allSorted.addElement(summ);
        numSentences++;
    }

    private Vector append(Vector vector, Vector vector1)
    {
        if(vector.isEmpty())
        {
            return vector1;
        }
        for(Enumeration enumeration = vector1.elements(); enumeration.hasMoreElements(); vector.addElement(enumeration.nextElement())) { }
        return vector;
    }

    private void bondSentences(Vector vector)
    {
        for(int i = 0; i < allSorted.size(); i++)
        {
            Summarise summarise = (Summarise)allSorted.elementAt(i);
            Vector vector1 = summarise.getSentence();
            int j = makeSentenceBonds(vector1, vector, i);
            summarise.addBondsAfter(j);
            summ.addBondsBefore(j);
        }

    }

    private int makeSentenceBonds(Vector vector, Vector vector1, int i)
    {
        int j = 0;
        int k = 0;
        int l;
        for(l = 0; j < vector.size() && k < vector1.size() && l <= bond_thresh;)
        {
            String s = vector.elementAt(j).toString();
            String s1 = vector1.elementAt(k).toString();
            int i1 = s.compareTo(s1);
            if(i1 == 0)
            {
                l++;
                j++;
                k++;
            } else
            if(i1 < 0)
            {
                j++;
            } else
            {
                k++;
            }
        }

        return l < bond_thresh ? 0 : 1;
    }

    private void addFastSum(Vector vector, Summarise summarise)
    {
        bonded = new Hashtable();
        Hashtable hashtable = new Hashtable();
        String s;
        Vector vector1;
        for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements(); fastSum.put(s, vector1))
        {
            s = enumeration.nextElement().toString();
            vector1 = (Vector)fastSum.get(s);
            if(vector1 != null)
            {
                fastSum.remove(s);
                //System.out.println("Link: " + s);
                Enumeration enumeration1 = vector1.elements();
                do
                {
                    if(!enumeration1.hasMoreElements())
                    {
                        break;
                    }
                    Summarise summarise1 = (Summarise)enumeration1.nextElement();
                    int i = 1;
                    if(!hashtable.containsKey(summarise1))
                    {
                        if(bonded.containsKey(summarise1))
                        {
                            i = ((Integer)bonded.get(summarise1)).intValue();
                            bonded.remove(summarise1);
                            i++;
                        }
                        bonded.put(summarise1, new Integer(i));
                        if(i >= bond_thresh)
                        {
                            summarise1.addBondsAfter(1);
                            summarise.addBondsBefore(1);
                            hashtable.put(summarise1, new Integer(i));
                        }
                    }
                } while(true);
            } else
            {
                vector1 = new Vector();
            }
            vector1.addElement(summarise);
        }

    }

    private Vector createFerretedWords(Vector vector)
    {
        Vector vector1 = new Vector();
        String s = "";
        for(int i = 0; i < vector.size(); i++)
        {
            String s1 = vector.elementAt(i).toString().toLowerCase().trim();
            if(s1.length() <= 0 || punctuation(s1) || whitespace(s1))
            {
                continue;
            }
            if(!closedClassWords.contains(s1))
            {
                if(s1.length() > 0)
                {
                    s = s + " ";
                }
                s = s + s1;
                continue;
            }
            int k = 0;
            s = stripPunctuation(s.trim());
            if(s.length() <= 0 || s.indexOf(" ") == -1)
            {
                continue;
            }
            for(; k < vector1.size() && s.compareTo(vector1.elementAt(k).toString()) > 0; k++) { }
            vector1.insertElementAt(s, k);
            s = "";
        }

        if(s.length() > 0)
        {
            int j = 0;
            s = stripPunctuation(s.trim());
            if(s.length() > 0 && s.indexOf(" ") != -1)
            {
                for(; j < vector1.size() && s.compareTo(vector1.elementAt(j).toString()) > 0; j++) { }
                vector1.insertElementAt(s, j);
            }
        }
        return vector1;
    }

    private Vector createStoredWords(Vector vector)
    {
        Vector vector1 = new Vector();
        for(int i = 0; i < vector.size(); i++)
        {
            String s = vector.elementAt(i).toString().toLowerCase().trim();
            if(s.length() <= 0 || punctuation(s) || whitespace(s) || closedClassWords.contains(s))
            {
                continue;
            }
            int j = 0;
            s = stripPunctuation(s);
            if(s.length() <= 0)
            {
                continue;
            }
            for(; j < vector1.size() && s.compareTo(vector1.elementAt(j).toString()) > 0; j++) { }
            vector1.insertElementAt(s, j);
        }

        return vector1;
    }

    private String stripPunctuation(String s)
    {
        if(s.length() > 0)
        {
            String s1 = s.substring(s.length() - 1, s.length());
            if(punctuation(s1))
            {
                return stripPunctuation(s.substring(0, s.length() - 1));
            } else
            {
                return s;
            }
        } else
        {
            return s;
        }
    }

    private boolean punctuation(String s)
    {
        return s.equals("[") || s.equals("]") || s.equals("{") || s.equals("}") || s.equals(";") || s.equals(":") || s.equals("$") || s.equals("(") || s.equals(")") || s.equals(",") || s.equals("+") || s.equals("-") || s.equals(".") || s.equals("\"") || s.equals("'") || s.equals("`");
    }

    private boolean whitespace(String s)
    {
        return s.equals(" ") || s.equals("\n") || s.equals("\t");
    }

    private void printSentencesInfo(Vector vector)
    {
        Enumeration enumeration = fastSum.keys();
        Enumeration enumeration1 = fastSum.elements();
        for(; enumeration.hasMoreElements(); System.out.println(enumeration.nextElement().toString() + "\t" + ((Vector)enumeration1.nextElement()).size())) { }
        System.out.println("----------------");
        for(int i = 0; i < vector.size(); i++)
        {
            Summarise summarise = (Summarise)vector.elementAt(i);
            System.out.print("" + summarise.index + "\t" + summarise.bonds_before() + "\t" + summarise.bonds_after() + " ");
            System.out.println();
        }

    }

    private void determine_output()
    {
        StringBuffer stringbuffer = new StringBuffer();
        Vector vector = new Vector();
        Vector vector1 = new Vector();
        for(int i = 0; i < allSorted.size(); i++)
        {
            Summarise summarise = (Summarise)allSorted.elementAt(i);
            int k;
            for(k = 0; k < vector.size() && ((Summarise)vector.elementAt(k)).bonds_diff() < summarise.bonds_diff(); k++) { }
            vector.insertElementAt(summarise, k);
        }

        for(int j = 0; j < allSorted.size(); j++)
        {
            Summarise summarise1 = (Summarise)allSorted.elementAt(j);
            int l;
            for(l = 0; l < vector1.size() && summarise1.bonds_total() < ((Summarise)vector1.elementAt(l)).bonds_total(); l++) { }
            vector1.insertElementAt(summarise1, l);
        }

        double d = allSorted.size();
        int i1 = (int)((top * d) / 100D + 0.5D);
        int j1 = (int)((mid * d) / 100D + 0.5D);
        int k1 = (int)((bottom * d) / 100D + 0.5D);
        Vector vector2 = new Vector();
        int l1 = allSorted.size() - i1;
        for(int i2 = l1; i2 < vector.size(); i2++)
        {
            Summarise summarise2 = (Summarise)vector.elementAt(i2);
            int j3;
            for(j3 = 0; j3 < vector2.size() && summarise2.index > ((Summarise)vector2.elementAt(j3)).index; j3++) { }
            vector2.insertElementAt(summarise2, j3);
            vector1.removeElement(summarise2);
        }

        for(int j2 = l1; j2 < vector.size(); j2++)
        {
            vector.removeElementAt(j2);
        }

        for(int k2 = 0; k2 < j1; k2++)
        {
            Summarise summarise3 = (Summarise)vector1.elementAt(k2);
            int k3;
            for(k3 = 0; k3 < vector2.size() && summarise3.index > ((Summarise)vector2.elementAt(k3)).index; k3++) { }
            vector2.insertElementAt(summarise3, k3);
            vector.removeElement(summarise3);
        }

        for(int l2 = 0; l2 < k1; l2++)
        {
            Summarise summarise4 = (Summarise)vector.elementAt(l2);
            int l3;
            for(l3 = 0; l3 < vector2.size() && summarise4.index > ((Summarise)vector2.elementAt(l3)).index; l3++) { }
            vector2.insertElementAt(summarise4, l3);
            vector1.removeElement(summarise4);
        }

        for(int i3 = 0; i3 < vector2.size(); i3++)
        {
            Summarise summarise5 = (Summarise)vector2.elementAt(i3);
            Vector vector3 = summarise5.getActualSentence();
            for(int i4 = 0; i4 < vector3.size(); i4++)
            {
                String s = vector3.elementAt(i4).toString();
                if(on_web)
                {
                    if(i4 > 0)
                    {
                        stringbuffer.append(" ");
                    }
                    stringbuffer.append(s);
                } else
                {
                    System.out.print(s + " ");
                }
            }

            if(on_web)
            {
                stringbuffer.append(nl);
            } else
            {
                System.out.println("");
            }
        }

        result = stringbuffer.toString();
    }

    private void setClosedClass(String s)
    {
        closedClassWords = new Vector();
        boolean flag = false;
        if(!s.endsWith(nl))
        {
            s = s + nl;
        }
        int i;
        for(; s.length() > 0 && (i = s.indexOf(nl)) > -1; s = s.substring(i + 1))
        {
            closedClassWords.addElement(s.substring(0, i).trim());
        }

    }

    public void setIgnoreWords(String s)
    {
        setClosedClass(s);
    }

    public String getTextSummary(String s)
    {
        int i = 0;
        SentenceStorer sentencestorer = new SentenceStorer();
        splitText(sentencestorer, s, i);
        determine_output();
        return result;
    }

    public void summariseText(String s, String s1)
    {
        int i = 0;
        SentenceStorer sentencestorer = new SentenceStorer();
        setClosedClass(s1);
        splitText(sentencestorer, s, i);
        determine_output();
    }

    private void splitText(SentenceStorer sentencestorer, String s, int i)
    {
        if(!s.endsWith(nl))
        {
            s = s + nl;
        }
        int j;
        for(; s.length() > 0 && (j = s.indexOf(nl)) > -1; s = s.substring(j + 1))
        {
            String s1 = s.substring(0, j);
            if(s1.length() <= 0)
            {
                continue;
            }
            sentencestorer.addLine(s1, false);
            if(!sentencestorer.eos_found())
            {
                continue;
            }
            Vector vector = sentencestorer.getAllSentences();
            for(int k = 0; k < vector.size(); k++)
            {
                Vector vector1 = (Vector)vector.elementAt(k);
                addSentence(vector1, i);
                i++;
            }

            sentencestorer.doneSentences();
        }

    }
}
