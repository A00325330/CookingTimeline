package com.tus.group_project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UE {
	@Id
	private Integer tac;
	
	@Column
	private String marketingName;
	
	@Column
	private String manufacturer;
	
	@Column
	@Lob
	private String accessCapability;
	
	@Column
	private String model;
	
	@Column
	private String vendorName;
	
	@Column
	private String ueType;
	
	@Column
	private String os;
	
	@Column 
	private String inputMode;
}
