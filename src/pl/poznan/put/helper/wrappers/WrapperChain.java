package pl.poznan.put.helper.wrappers;

import org.biojava.bio.structure.Chain;

import pl.poznan.put.helper.StructureManager;

public class WrapperChain {
    private Chain chain;

    public WrapperChain(Chain chain) {
        this.chain = chain;
    }

    public Chain getChain() {
        return chain;
    }

    @Override
    public String toString() {
        return StructureManager.getName(chain.getParent()) + "."
                + chain.getChainID();
    }
}
