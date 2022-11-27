package io._3650.jeidescs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class JEIDUtil {
	
	public static <T> List<T> optionalJsonArray(JsonElement json, Function<JsonElement, T> func) {
		if (json instanceof JsonArray arr) {
			ArrayList<T> result = new ArrayList<>(arr.size());
			for (var elem : arr) {
				T t = func.apply(elem);
				if (t != null) result.add(t); //allow items to be skipped by returning null
			}
			return result;
		} else {
			T t = func.apply(json);
			if (t != null) return List.of(t);
			else return List.of();
		}
	}
	
	public static void forEachOptionalJsonArray(JsonElement json, Consumer<JsonElement> consumer) {
		if (json instanceof JsonArray arr) {
			for (var elem : arr) consumer.accept(elem);
		} else consumer.accept(json);
	}
	
	public static <T> Holder<T> holder(T value) {
		return new Holder<>(value);
	}
	
	public static class Holder<T> {
		public T value;
		
		public Holder(T value) {
			this.value = value;
		}
	}
	
}