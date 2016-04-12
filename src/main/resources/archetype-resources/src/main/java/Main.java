package ${package};

import enkan.system.devel.DevelCommandRegister;
import enkan.system.repl.PseudoRepl;
import enkan.system.repl.ReplBoot;
import kotowari.scaffold.command.ScaffoldCommandRegister;
import kotowari.system.KotowariCommandRegister;

/**
 * @author kawasima
 */
public class Main {
    public static void main(String[] args) {
        PseudoRepl repl = new PseudoRepl(MyExampleSystemFactory.class.getName());
        ReplBoot.start(repl,
                new ScaffoldCommandRegister(),
                new KotowariCommandRegister(),
                new DevelCommandRegister());
    }
}
