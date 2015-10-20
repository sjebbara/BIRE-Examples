package templates;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import changes.StateChange;
import corpus.AnnotatedDocument;
import factors.AbstractFactor;
import factors.impl.UnorderedEntititesFactor;
import learning.ObjectiveFunction;
import learning.Vector;
import objective.DefaultObjectiveFunction;
import templates.AbstractTemplate;
import variables.State;

public class CheatingTemplate extends AbstractTemplate<State>implements Serializable {

	private static Logger log = LogManager.getFormatterLogger(CheatingTemplate.class.getName());
	private static final String GOLD = "GOLD";

	private ObjectiveFunction<State> objective = new DefaultObjectiveFunction();

	public CheatingTemplate() {
	}

	@Override
	public void computeFactor(State state, AbstractFactor factor) {
		Vector featureVector = new Vector();
		if (state.getDocument() instanceof AnnotatedDocument) {
			State goldState = ((AnnotatedDocument<State>) state.getDocument()).getGoldState();
			double score = objective.score(state, goldState);
			featureVector.set(GOLD, score);

			factor.setFeatures(featureVector);
		} else {
			log.warn(
					"Template %s: Given state does not have an AnnotatedDocument attached. Cheating template not applicable.",
					this.getClass().getSimpleName());
		}
	}

	@Override
	protected boolean isRelevantChange(StateChange value) {
		return true;
	}

	@Override
	protected Set<AbstractFactor> generateFactors(State state) {
		Set<AbstractFactor> factors = new HashSet<>();
		factors.add(new UnorderedEntititesFactor(this, state.getEntityIDs()));
		return factors;
	}
}