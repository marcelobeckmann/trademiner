package com.rapidminer.operator.preprocessing.transformation;



import java.util.Vector;
public class Summarise
{

    public int index;
    int bonds_before;
    int bonds_after;
    Vector sentence;
    Vector actual;

    Summarise(Vector vector, Vector vector1, int i)
    {
        bonds_before = 0;
        bonds_after = 0;
        actual = vector;
        sentence = vector1;
        index = i;
    }

    public void addBondsAfter(int i)
    {
        bonds_after += i;
    }

    public void addBondsBefore(int i)
    {
        bonds_before += i;
    }

    public int bonds_diff()
    {
        return bonds_before - bonds_after;
    }

    public int bonds_total()
    {
        return bonds_before + bonds_after;
    }

    public int bonds_before()
    {
        return bonds_before;
    }

    public int bonds_after()
    {
        return bonds_after;
    }

    public Vector getActualSentence()
    {
        return actual;
    }

    public Vector getSentence()
    {
        return sentence;
    }
}
