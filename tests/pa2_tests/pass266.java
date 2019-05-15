// PA2 pass AST reflects associativity of nested if stmts
class B {
    public void foo() {
        // nested if stmts should associate in order suggested
        // by indentation

        if (b)
            if (c)
                x = 1;
            else
                x = 2;
        else
            if (d)
                x = 11;
            else
                x = 22;


        if (true)
            if (false)
                x = 3;
            else
                x = 4;

       
        if (!true)
            x = 33;
        else
            if (!false)
                x = 44;
            else
                x = 55;
    }
}
