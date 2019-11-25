	Program Survivability
****** 
      parameter (ng=2,nv=2,nh=2,nm=ng+nh+nv) ! 2g ring
      integer(8) m,nt,in,Etot,Eact,l,nc,SNM
      integer(8) X(nm),P(nm),E(nm),F(nm),S(nm),R(nm),N(nm)
      integer(8) Y(nm,nm),Z(nm,nm),O(nm),C(nm)
      real(8)    PS,PR,PF,p1,p2,p3,pt 
****** nm is the total number of elements
****** ng is the number of generators
****** nv is the number of vertical links
****** nh is the number of horizontal links

****** we represent a generator bus as a graph X, and then 
****** each element is a graph element X(i). The elements X(1)...X(ng)
****** are generators, X(ng+1)...X(ng+nv) are vertical links, and  
****** X(ng+nv+1)...X(nm) are horizontal links.

****** Map Y(i,j) of the graph describes the connection between different 
****** elements: Y(i,j)=1 means that the elements X(i) and X(j) are 
****** connected, if Y(i,j)=0, it means that they are not
      Y=0
      call topology(nm,Y)

****** we assign to each element some quality E (here, power). 
****** If the i-element of the X-graph is a generator, E(i)=1;
****** E(i)=-1 if it is a vertical link, and E(i)=0 if it is a horizontal link

      do i=1,nm
	  E(i)=0
	  if(i.le.ng) E(i)=1
	  if(i.gt.ng.and.i.le.ng+nv) E(i)=-1
	  enddo

****** Each graph element can be in "on" (available) or "off" (unavailable) 
****** state. If the i-element of the X-graph is in "on"-state,  
****** we assign it P(i)=1, P(i)=0 if it is unavailable.

****** nt is the total number of combinations of available/unavailable elements.
****** m  is the number of damaged (unavailable) elements

****** Etot is the power produced initially by all available generators
****** Eact is the power available to the rest of IPS after damage 

      Etot=ng

****** F(m) is the number of combinations (scenarios) leading to Eact=0
****** S(m) is the number of combinations (scenarios) leading to Eact=Etot
****** R(m) is the number of combinations (scenarios) leading to Eact<Etot
****** N(m) is the total number of scenarios at a given m (m runs from 1 to nm)

      nt=2**nm
      F=0
	  S=0
	  R=0
      N=0
****** Step 1: generate a combination of available/unavailable elements	
      do 5 k=1,nt
      write(*,*) 'nt=',nt,'k=',k
      P=0
	  in=k-1
      
	  Eact=Etot
    
	  do i=1,nm
      l=in/2
      P(i)=in-2*l
      in=l
      enddo

****** Now we check how many elements are damaged in the combination
*      write(*,*) 'k=',k

      m=0
      do i=1,nm
      if(P(i).eq.0)m=m+1 
      enddo
      if(m.eq.0) goto 5
****** Z(i,j) reflects the connection between elements after damage
      Z=0
      do i=1,nm
	  do j=1,nm
      Z(i,j)=Y(i,j)*P(i)*P(j)
	  enddo
	  enddo

****** Step 2: we check how much power is available for the rest of IPS    
****** that is, how many generators are available and connected to
****** vertical links
      do 1 i=1,ng
	  if(P(i).eq.0) then
	  Eact=Eact-E(i)
	  goto 1
      endif

****** Here we check whether an available generator (X(1)..X(ng)) 
****** is connected to a vertical link (X(ng+1)..X(ng+nv))
      nc=0
      C=0
      O=0
******
	  j1=1
      C(j1)=i
	  k1=i
      j2=0

  3   continue

	  do 2 k2=1,nm
      do jj1=1,nm
      if(k2.eq.C(jj1).or.k2.eq.O(jj1)) goto 2
	  enddo
 
	  if(Z(k1,k2).eq.1)then

	  if(k1.ge.ng+1.and.k1.le.ng+nv)then
      nc=1
	  goto 1 
	  endif
	  if(k2.ge.ng+1.and.k2.le.ng+nv)then
      nc=1
	  goto 1 
	  endif

      j2=j2+1
      O(j2)=k2
      endif

  2	  continue
      
      if(j2.gt.0)then
	  k1=O(j2)
      j1=j1+1
      C(j1)=O(j2)
	  O(j2)=0
      j2=j2-1
      goto 3
	  endif

****** If there is no connection, then Eact is decreased
      if(nc.eq.0)	Eact=Eact-E(i)
  1   continue

****** The end of Step 2

	  if(Eact.eq.Etot) S(m)=S(m)+1
	  if(Eact.eq.0) F(m)=F(m)+1
	  if(Eact.gt.0.and.Eact.lt.Etot) R(m)=R(m)+1
	
	  N(m)=N(m)+1
****** The end of Step 1, generate the next combination of available/unavailable elements
****** or quit the program
  5   continue

      SNM=1
      do m=1,nm
      if(S(m)+R(m)+F(m).ne.N(m)) then
      write(*,*) 'ERROR at m=',m
	  endif
      SNM=N(m)+SNM
	  enddo
      if(SNM.ne.nt) then
      write(*,*) 'ERROR'
	  endif

      open(14,File='Scenarios.dat')
      write(14,*) 'm:S,R,F,N'

	  open(15,File='Probabilities.dat')
      write(15,*) 'm:P(S),P(R),P(F)'

      do m=1,nm
      p1=S(m)
	  p2=R(m)
	  p3=F(m)
	  pt=N(m)
	  PS=p1/pt
	  PR=p2/pt
	  PF=p3/pt
      write(14,92) m,S(m),R(m),F(m),N(m)
	  write(15,93) m,PS,PR,PF
      enddo

	  close(14)
	  close(15)

  92  format(i4,4i20)		    
  93  format(i4,3f10.3)

      stop
	  end

      subroutine topology(nm,Y)
	  integer(8) Y(nm,nm)
****** This topology represents a ring with two generators
****** generators are nodes 1 and 2 and vertical links are nodes 3 and 4
      Y(1,3)=1
      Y(1,5)=1
      Y(1,6)=1 
      Y(2,4)=1
      Y(2,5)=1
      Y(2,6)=1 
      Y(3,1)=1
      Y(5,1)=1
      Y(6,1)=1 
      Y(4,2)=1
      Y(5,2)=1
      Y(6,2)=1 
	do j=1,nm
	do i=j+1,nm
      Y(i,j)=Y(j,i)
	enddo
	enddo

	return
	end
