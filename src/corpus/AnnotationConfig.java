package corpus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import variables.EntityType;

public class AnnotationConfig implements Serializable {

	private Map<String, EntityType> entityTypes = new HashMap<String, EntityType>();

	public Collection<EntityType> getEntityTypes() {
		return new ArrayList<EntityType>(entityTypes.values());
	}

	public void addEntityType(EntityType entityType) {
		entityTypes.put(entityType.getName(), entityType);
	}

	public EntityType getEntityType(String type) {
		return entityTypes.get(type);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (EntityType entityType : entityTypes.values()) {
			b.append(entityType);
			b.append("\n");
		}
		return "AnnotationConfig:\n" + b;
	}

}