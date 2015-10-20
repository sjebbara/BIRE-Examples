package objective;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multimap;

import learning.ObjectiveFunction;
import utility.VariableID;
import variables.AbstractEntityAnnotation;
import variables.ArgumentRole;
import variables.State;

public class DefaultObjectiveFunction extends ObjectiveFunction<State> {

	private static Logger log = LogManager.getFormatterLogger(DefaultObjectiveFunction.class.getName());

	public DefaultObjectiveFunction() {
	}

	@Override
	public double computeScore(State state, State goldState) {
		Collection<AbstractEntityAnnotation> entities = state.getEntities();
		Collection<AbstractEntityAnnotation> goldEntities = goldState.getEntities();
		double f1Score;
		if (goldEntities.size() == 0 && entities.size() == 0) {
			f1Score = 1;
		} else {
			double precision = 0.0;
			for (AbstractEntityAnnotation entity : entities) {
				double max = 0.0;
				for (AbstractEntityAnnotation goldEntity : goldEntities) {
					if (typeMatches(entity, goldEntity)) {
						double overlapScore = overlapScore(entity, goldEntity);
						// FIXME comparing overlapScore to previous overlapScore
						// x argumentScore ??? Seems wrong!
						if (overlapScore > max) {
							max = overlapScore * argumentScore(entity, goldEntity);
						}
					}
				}
				precision += max;
			}

			double recall = 0.0;
			for (AbstractEntityAnnotation goldEntity : goldEntities) {
				double max = 0.0;
				for (AbstractEntityAnnotation entity : entities) {
					if (typeMatches(goldEntity, entity)) {
						double overlapScore = overlapScore(goldEntity, entity);
						if (overlapScore > max) {
							max = overlapScore * argumentScore(goldEntity, entity);
						}
					}
				}
				recall += max;
			}
			// TODO score = 0 only because precision/recall = 0
			if ((precision == 0 && recall == 0) || entities.size() == 0 || goldEntities.size() == 0) {
				f1Score = 0;
			} else {
				precision /= entities.size();
				recall /= goldEntities.size();

				f1Score = 2 * (precision * recall) / (precision + recall);
			}
		}
		return f1Score;
	}

	private double argumentScore(AbstractEntityAnnotation entity1, AbstractEntityAnnotation entity2) {
		Multimap<ArgumentRole, VariableID> arguments1 = entity1.getArguments();
		Multimap<ArgumentRole, VariableID> arguments2 = entity2.getArguments();

		if (arguments1.keySet().size() == 0)
			return 1;

		int matchingRoles = 0;

		// count arguments of entity1 that are also in entity2
		for (Entry<ArgumentRole, VariableID> argument1 : arguments1.entries()) {
			ArgumentRole argRole1 = argument1.getKey();
			AbstractEntityAnnotation argEntity1 = entity1.getEntity(argument1.getValue());
			/*
			 * Since there are possibly several arguments with the same role,
			 * check if there is at least one that matches (overlaps)
			 * argEntity1.
			 */
			Collection<VariableID> argsForRole2 = arguments2.get(argRole1);
			for (VariableID argForRoleEntityID2 : argsForRole2) {
				AbstractEntityAnnotation argEntity2 = entity2.getEntity(argForRoleEntityID2);
				if (overlapScore(argEntity1, argEntity2) > 0) {
					matchingRoles++;
					// only count one match per argument
					break;
				}
			}
		}
		return matchingRoles / arguments1.size();
	}

	public static double overlapScore(AbstractEntityAnnotation entity, AbstractEntityAnnotation goldEntity) {
		int a = entity.getBeginTokenIndex();
		int b = entity.getEndTokenIndex();
		int x = goldEntity.getBeginTokenIndex();
		int y = goldEntity.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x));
		double overlapScore = ((double) overlap) / (b - a);
		return overlapScore;
	}

	public static int overlap(AbstractEntityAnnotation entity1, AbstractEntityAnnotation entity2) {
		int a = entity1.getBeginTokenIndex();
		int b = entity1.getEndTokenIndex();
		int x = entity2.getBeginTokenIndex();
		int y = entity2.getEndTokenIndex();
		int overlap = Math.max(0, Math.min(b, y) - Math.max(a, x));
		return overlap;
	}

	public boolean typeMatches(AbstractEntityAnnotation entity, AbstractEntityAnnotation goldEntity) {
		return entity.getType().equals(goldEntity.getType());
	}

}
